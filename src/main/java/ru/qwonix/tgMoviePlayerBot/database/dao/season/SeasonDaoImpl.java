package ru.qwonix.tgMoviePlayerBot.database.dao.season;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeasonDaoImpl implements SeasonDao {

    private final ConnectionPool connectionPool;

    public SeasonDaoImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Season convert(ResultSet resultSet) throws SQLException {
        SeriesDao seriesDao = new SeriesDaoImpl(connectionPool);
        Optional<Series> series = seriesDao.find(resultSet.getInt("series_id"));

        LocalDate premiereReleaseDate = this.findPremiereReleaseDate(resultSet.getInt("id"));
        LocalDate finalReleaseDate = this.findFinalReleaseDate(resultSet.getInt("id"));
        return Season.builder()
                .id(resultSet.getInt("id"))
                .number(resultSet.getInt("number"))
                .description(resultSet.getString("description"))
                .totalEpisodesCount(resultSet.getInt("total_episodes_count"))
                .previewTgFileId(resultSet.getString("preview_tg_file_id"))
                .series(series.orElse(null))
                .premiereReleaseDate(premiereReleaseDate)
                .finalReleaseDate(finalReleaseDate)
                .build();
    }


    private LocalDate findPremiereReleaseDate(int seasonId) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select min(release_date) as premiere_release_date from episode where season_id = ?")) {
            preparedStatement.setInt(1, seasonId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            return resultSet.getObject("premiere_release_date", LocalDate.class);
        } finally {
            connectionPool.releaseConnection(connection);
        }
    }

    private LocalDate findFinalReleaseDate(int seasonId) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select" +
                             " case" +
                             "   when (select count(*) from episode where season_id = ?) =" +
                             "   (select total_episodes_count from season where id = ?)" +
                             " then (select max(release_date) from episode where season_id = ?)" +
                             " end as final_release_date")) {
            preparedStatement.setInt(1, seasonId);
            preparedStatement.setInt(2, seasonId);
            preparedStatement.setInt(3, seasonId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getObject("final_release_date", LocalDate.class);
        } finally {
            connectionPool.releaseConnection(connection);
        }
    }

    @Override
    public List<Season> findAllBySeriesOrderByNumberWithLimitAndPage(Series series, int limit, int page) throws SQLException {
        Connection connection = connectionPool.getConnection();

        List<Season> seasons = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM season where series_id = ? order by number limit ? offset ?")) {
            preparedStatement.setInt(1, series.getId());
            preparedStatement.setInt(2, limit);
            preparedStatement.setInt(3, page);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Season season = convert(resultSet);
                seasons.add(season);
            }
        } finally {
            connectionPool.releaseConnection(connection);
        }
        return seasons;
    }

    @Override
    public int countAllBySeries(Series series) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT count(*) as count FROM season where series_id = ?")) {
            preparedStatement.setInt(1, series.getId());

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            return resultSet.getInt("count");
        } finally {
            connectionPool.releaseConnection(connection);
        }
    }

    @Override
    public Optional<Season> find(long id) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM season WHERE id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Season season = convert(resultSet);
                return Optional.of(season);
            }
        } finally {
            connectionPool.releaseConnection(connection);
        }

        return Optional.empty();
    }


}
