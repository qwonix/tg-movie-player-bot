package ru.qwonix.tgMoviePlayerBot.database.dao.episode;

import org.postgresql.util.PGInterval;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.season.SeasonDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.season.SeasonDaoImpl;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Video;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class EpisodeDaoImpl implements EpisodeDao {
    private final ConnectionBuilder connectionBuilder;

    public EpisodeDaoImpl(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    @Override
    public Episode convert(ResultSet episodeResultSet) throws SQLException {
        SeasonDao seasonDao = new SeasonDaoImpl(connectionBuilder);
        VideoDao videoDao = new VideoDaoImpl(connectionBuilder);

        Optional<Season> season = seasonDao.find(episodeResultSet.getInt("season_id"));
        List<Video> videos = videoDao.findAllByEpisodeId(episodeResultSet.getInt("id"));
        PGInterval duration = (PGInterval) episodeResultSet.getObject("duration");
        return Episode.builder()
                .id(episodeResultSet.getInt("id"))
                .number(episodeResultSet.getInt("number"))
                .productionCode(episodeResultSet.getInt("production_code"))
                .title(episodeResultSet.getString("title"))
                .description(episodeResultSet.getString("description"))
                .releaseDate(episodeResultSet.getObject("release_date", LocalDate.class))
                .language(episodeResultSet.getString("language"))
                .country(episodeResultSet.getString("country"))
                .duration(Duration.ofSeconds(duration.getWholeSeconds()))
                .season(season.orElse(null))
                .videos(videos)
                .previewFileId(episodeResultSet.getString("tg_preview_file_id"))
                .build();
    }

    @Override
    public List<Episode> findAll() throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Episode> episodes = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String SQL = "SELECT * FROM episode";
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                Episode episode = convert(resultSet);
                episodes.add(episode);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return episodes;
    }

    @Override
    public Optional<Episode> find(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM episode WHERE id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Episode episode = convert(resultSet);
                return Optional.of(episode);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Episode> findNext(long episodeId, long seasonId) throws SQLException {
        Iterator<Episode> iterator = this.findAllBySeasonIdOrderByNumberAsc(seasonId).iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId() == episodeId) {
                return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Episode> findPrevious(long episodeId, long seasonId) throws SQLException {
        List<Episode> allBySeasonIdOrderByNumberDesc = this.findAllBySeasonIdOrderByNumberDesc(seasonId);

        Iterator<Episode> iterator = allBySeasonIdOrderByNumberDesc.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId() == episodeId) {
                return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Episode> findAllBySeasonOrderByNumberWithLimitAndPage(long seasonId, int limit, int page) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

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
            connectionBuilder.releaseConnection(connection);
        }
        return episodes;
    }

    @Override
    public List<Episode> findAllBySeasonIdOrderByNumberAsc(long seasonId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Episode> episodes = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM episode where season_id=? order by number asc")) {
            preparedStatement.setLong(1, seasonId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Episode episode = convert(resultSet);
                episodes.add(episode);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return episodes;
    }

    @Override
    public List<Episode> findAllBySeasonIdOrderByNumberDesc(long seasonId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Episode> episodes = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM episode where season_id=? order by number desc")) {
            preparedStatement.setLong(1, seasonId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Episode episode = convert(resultSet);
                episodes.add(episode);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return episodes;
    }

    @Override
    public int countAllBySeasonId(long seasonId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT count(*) as count FROM episode where season_id=?")) {
            preparedStatement.setLong(1, seasonId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt("count");
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public Optional<Episode> findLast(int seasonId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM episode where season_id=? order by number desc limit 1")) {
            preparedStatement.setLong(1, seasonId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Episode episode = convert(resultSet);
                return Optional.of(episode);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Episode> findByVideo(int videoId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM episode where id = (select episode_id from video where id=?)")) {
            preparedStatement.setLong(1, videoId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Episode episode = convert(resultSet);
                return Optional.of(episode);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return Optional.empty();
    }

    @Override
    public void insert(Episode episode) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        PGInterval interval = new PGInterval();
        interval.setSeconds(episode.getDuration().getSeconds());
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("INSERT INTO episode (number, production_code, title, description, release_date, language, country, duration, season_id, tg_preview_file_id) " +
                             "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            preparedStatement.setInt(1, episode.getNumber());
            preparedStatement.setInt(2, episode.getProductionCode());
            preparedStatement.setString(3, episode.getTitle());
            preparedStatement.setString(4, episode.getDescription());
            preparedStatement.setObject(5, episode.getReleaseDate());
            preparedStatement.setString(6, episode.getLanguage());
            preparedStatement.setString(7, episode.getCountry());
            preparedStatement.setObject(8, interval);
            preparedStatement.setInt(9, episode.getSeason().getId());
            preparedStatement.setString(10, episode.getPreviewFileId());

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public void update(long id, Episode episode) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        PGInterval interval = new PGInterval();
        interval.setSeconds(episode.getDuration().getSeconds());
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("UPDATE episode " +
                             "SET number=?, production_code=?, title=?, description=?, release_date=?, language=?, country=?, duration=?, season_id=?, tg_preview_file_id=? WHERE id=?")) {
            preparedStatement.setInt(1, episode.getNumber());
            preparedStatement.setInt(2, episode.getProductionCode());
            preparedStatement.setString(3, episode.getTitle());
            preparedStatement.setString(4, episode.getDescription());
            preparedStatement.setObject(5, episode.getReleaseDate());
            preparedStatement.setString(6, episode.getLanguage());
            preparedStatement.setString(7, episode.getCountry());
            preparedStatement.setObject(8, interval);
            preparedStatement.setInt(9, episode.getSeason().getId());
            preparedStatement.setString(10, episode.getPreviewFileId());

            preparedStatement.setLong(11, id);

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public void delete(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("DELETE FROM series WHERE id=?")) {
            preparedStatement.setLong(1, id);

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }
}