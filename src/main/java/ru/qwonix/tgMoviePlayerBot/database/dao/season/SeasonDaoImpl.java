package ru.qwonix.tgMoviePlayerBot.database.dao.season;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeasonDaoImpl implements SeasonDao {

    private final ConnectionBuilder connectionBuilder;

    public SeasonDaoImpl(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    @Override
    public Season convert(ResultSet seasonResultSet) throws SQLException {
        SeriesDao seriesDao = new SeriesDaoImpl(connectionBuilder);
        Optional<Series> series = seriesDao.find(seasonResultSet.getInt("series_id"));

        LocalDate premiereReleaseDate = this.findPremiereReleaseDate(seasonResultSet.getInt("id"));
        LocalDate finalReleaseDate = this.findFinalReleaseDate(seasonResultSet.getInt("id"));
        return Season.builder()
                .id(seasonResultSet.getInt("id"))
                .number(seasonResultSet.getInt("number"))
                .description(seasonResultSet.getString("description"))
                .premiereReleaseDate(premiereReleaseDate)
                .finalReleaseDate(finalReleaseDate)
                .totalEpisodesCount(seasonResultSet.getInt("total_episodes_count"))
                .previewFileId(seasonResultSet.getString("tg_preview_file_id"))
                .series(series.orElse(null))
                .build();
    }

    @Override
    public List<Season> findAll() throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Season> seasons = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String SQL = "SELECT * FROM season";
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                Season season = convert(resultSet);
                seasons.add(season);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return seasons;
    }


    @Override
    public LocalDate findPremiereReleaseDate(int seasonId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select min(release_date) as premiere_release_date from episode where season_id = ?")) {
            preparedStatement.setInt(1, seasonId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            return resultSet.getObject("premiere_release_date", LocalDate.class);
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public LocalDate findFinalReleaseDate(int seasonId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

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
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public List<Season> findAllBySeriesOrderByNumberWithLimitAndPage(Series series, int limit, int page) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

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
            connectionBuilder.releaseConnection(connection);
        }
        return seasons;
    }

    @Override
    public int countAllBySeries(Series series) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT count(*) as count FROM season where series_id = ?")) {
            preparedStatement.setInt(1, series.getId());

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            return resultSet.getInt("count");
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public Optional<Season> find(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM season WHERE id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Season season = convert(resultSet);
                return Optional.of(season);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

        return Optional.empty();
    }

    @Override
    public void insert(Season season) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("INSERT INTO season (number, description, total_episodes_count, series_id, tg_preview_file_id) " +
                             "VALUES(?, ?, ?, ?, ?)")) {

            preparedStatement.setInt(1, season.getNumber());
            preparedStatement.setString(2, season.getDescription());
            preparedStatement.setObject(3, season.getTotalEpisodesCount());
            preparedStatement.setInt(4, season.getSeries().getId());
            preparedStatement.setString(5, season.getPreviewFileId());

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public void update(long id, Season season) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("UPDATE season SET number=?, description=?, total_episodes_count=?, series_id=?, tg_preview_file_id=? WHERE id=?")) {

            preparedStatement.setInt(1, season.getNumber());
            preparedStatement.setString(2, season.getDescription());
            preparedStatement.setObject(3, season.getTotalEpisodesCount());
            preparedStatement.setLong(4, season.getSeries().getId());
            preparedStatement.setString(5, season.getPreviewFileId());
            preparedStatement.setLong(6, id);

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public void delete(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("DELETE FROM season WHERE id=?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }
}
