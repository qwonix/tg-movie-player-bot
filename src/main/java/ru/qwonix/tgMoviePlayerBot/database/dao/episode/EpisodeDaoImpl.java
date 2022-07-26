package ru.qwonix.tgMoviePlayerBot.database.dao.episode;

import org.postgresql.util.PGInterval;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.season.SeasonDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.season.SeasonDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
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
        Optional<Season> season = seasonDao.find(episodeResultSet.getInt("season_id"));

        PGInterval duration = (PGInterval) episodeResultSet.getObject("duration");
        return Episode.builder()
                .id(episodeResultSet.getInt("id"))
                .number(episodeResultSet.getInt("number"))
                .name(episodeResultSet.getString("name"))
                .description(episodeResultSet.getString("description"))
                .releaseDate(episodeResultSet.getObject("release_date", LocalDate.class))
                .language(episodeResultSet.getString("language"))
                .country(episodeResultSet.getString("country"))
                .duration(Duration.ofSeconds(duration.getWholeSeconds()))
                .season(season.orElse(null))
                .fileId(episodeResultSet.getString("telegram_file_id"))
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
    public List<Episode> findAllBySeason(Season season) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Episode> episodes = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM episode e " +
                             "inner join season s on s.id = e.season_id where s.id=?")) {
            preparedStatement.setLong(1, season.getId());

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
    public void insert(Episode episode) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("INSERT INTO episode (number, name, description, release_date, language, country, duration, season_id, telegram_file_id) " +
                             "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            preparedStatement.setInt(1, episode.getNumber());
            preparedStatement.setString(2, episode.getName());
            preparedStatement.setString(3, episode.getDescription());
            preparedStatement.setObject(4, episode.getReleaseDate());
            preparedStatement.setString(5, episode.getLanguage());
            preparedStatement.setString(6, episode.getCountry());
            preparedStatement.setObject(7, episode.getDuration());
            preparedStatement.setInt(8, episode.getSeason().getId());
            preparedStatement.setString(9, episode.getFileId());

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public void update(long id, Episode series) throws SQLException {
        Connection connection = connectionBuilder.getConnection();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("UPDATE episode " +
                             "SET number=?, name=?, description=?, release_date=?, language=?, country=?, duration=?, season_id=?, telegram_file_id=? WHERE id=?")) {
            preparedStatement.setInt(1, series.getNumber());
            preparedStatement.setString(2, series.getName());
            preparedStatement.setString(3, series.getDescription());
            preparedStatement.setObject(4, series.getReleaseDate());
            preparedStatement.setString(5, series.getLanguage());
            preparedStatement.setString(6, series.getCountry());
            preparedStatement.setObject(7, series.getDuration());
            preparedStatement.setInt(8, series.getSeason().getId());
            preparedStatement.setString(9, series.getFileId());
            preparedStatement.setLong(10, id);

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