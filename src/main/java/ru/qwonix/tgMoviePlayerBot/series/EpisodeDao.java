package ru.qwonix.tgMoviePlayerBot.series;

import org.postgresql.util.PGInterval;
import ru.qwonix.tgMoviePlayerBot.dao.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EpisodeDao {
    private final ConnectionBuilder connectionBuilder;

    public EpisodeDao(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    private Episode convert(ResultSet episodeResultSet) throws SQLException {
        PGInterval duration = (PGInterval) episodeResultSet.getObject("duration");

        SeasonDao seasonDao = new SeasonDao(connectionBuilder);
        Optional<Season> season = seasonDao.find(episodeResultSet.getInt("season_id"));

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
        }

        finally {
            connectionBuilder.releaseConnection(connection);
        }
        return episodes;
    }

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
        }
        finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    public Optional<Episode> find(int id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM episode WHERE id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Episode episode = convert(resultSet);
                return Optional.of(episode);
            }
        }
        finally {
            connectionBuilder.releaseConnection(connection);
        }

        return Optional.empty();
    }
}