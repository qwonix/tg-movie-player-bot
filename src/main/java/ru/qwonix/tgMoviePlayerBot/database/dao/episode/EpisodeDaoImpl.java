package ru.qwonix.tgMoviePlayerBot.database.dao.episode;

import org.postgresql.util.PGInterval;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.dao.season.SeasonDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.season.SeasonDaoImpl;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Video;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EpisodeDaoImpl implements EpisodeDao {
    private final ConnectionPool connectionPool;

    public EpisodeDaoImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Episode convert(ResultSet resultSet) throws SQLException {
        SeasonDao seasonDao = new SeasonDaoImpl(connectionPool);
        VideoDao videoDao = new VideoDaoImpl(connectionPool);

        Optional<Season> season = seasonDao.find(resultSet.getInt("season_id"));
        List<Video> videos = videoDao.findAllByEpisodeId(resultSet.getInt("id"));
        PGInterval duration = (PGInterval) resultSet.getObject("duration");
        return Episode.builder()
                .id(resultSet.getInt("id"))
                .number(resultSet.getInt("number"))
                .productionCode(resultSet.getInt("production_code"))
                .title(resultSet.getString("title"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getObject("release_date", LocalDate.class))
                .language(resultSet.getString("language"))
                .country(resultSet.getString("country"))
                .duration(Duration.ofSeconds(duration.getWholeSeconds()))
                .previewTgFileId(resultSet.getString("preview_tg_file_id"))
                .videos(videos)
                .season(season.orElse(null))
                .build();
    }

    @Override
    public Optional<Episode> find(long id) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM episode WHERE id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Episode episode = convert(resultSet);
                return Optional.of(episode);
            }
        } finally {
            connectionPool.releaseConnection(connection);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Episode> findNext(long episodeId) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select * " +
                             "from episode " +
                             "where number > (select number from episode where id = ?) " +
                             "and season_id = (select season_id from episode where id = ?) " +
                             "order by number limit 1")) {
            preparedStatement.setLong(1, episodeId);
            preparedStatement.setLong(2, episodeId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Episode episode = convert(resultSet);
                return Optional.of(episode);
            }
        } finally {
            connectionPool.releaseConnection(connection);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Episode> findPrevious(long episodeId, long seasonId) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select * " +
                             "from episode " +
                             "where number < (select number from episode where id = ?) " +
                             "and season_id = (select season_id from episode where id = ?) " +
                             "order by number desc limit 1")) {
            preparedStatement.setLong(1, episodeId);
            preparedStatement.setLong(2, episodeId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Episode episode = convert(resultSet);
                return Optional.of(episode);
            }
        } finally {
            connectionPool.releaseConnection(connection);
        }

        return Optional.empty();
    }

    @Override
    public List<Episode> findAllBySeasonOrderByNumberWithLimitAndPage(long seasonId, int limit, int page) throws SQLException {
        Connection connection = connectionPool.getConnection();

        List<Episode> episodes = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM episode where season_id=? order by number limit ? offset ?")) {
            preparedStatement.setLong(1, seasonId);
            preparedStatement.setInt(2, limit);
            preparedStatement.setInt(3, limit * page);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Episode episode = convert(resultSet);
                episodes.add(episode);
            }
        } finally {
            connectionPool.releaseConnection(connection);
        }
        return episodes;
    }

    @Override
    public int countAllBySeasonId(long seasonId) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT count(*) as count FROM episode where season_id=?")) {
            preparedStatement.setLong(1, seasonId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt("count");
        } finally {
            connectionPool.releaseConnection(connection);
        }
    }

    @Override
    public void setAvailableByEpisodeProductionCode(int episodeProductionCode, Boolean isAvailable) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("UPDATE episode_all set is_available=? where production_code = ?")) {
            preparedStatement.setBoolean(1, isAvailable);
            preparedStatement.setInt(2, episodeProductionCode);

            preparedStatement.executeUpdate();
        } finally {
            connectionPool.releaseConnection(connection);
        }
    }
}