package ru.qwonix.tgMoviePlayerBot.database.dao.video;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.entity.Video;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VideoDaoImpl implements VideoDao {
    private final ConnectionBuilder connectionBuilder;

    public VideoDaoImpl(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    @Override
    public Video convert(ResultSet episodeResultSet) throws SQLException {
        return Video.builder()
                .id(episodeResultSet.getInt("id"))
                .resolution(episodeResultSet.getInt("resolution"))
                .audioLanguage(episodeResultSet.getString("audio_language"))
                .subtitlesLanguage(episodeResultSet.getString("subtitles_language"))
                .videoFileId(episodeResultSet.getString("video_file_id"))
                .priority(episodeResultSet.getInt("priority"))
                .build();
    }

    @Override
    public List<Video> findAll() throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Video> videos = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String SQL = "SELECT * FROM video";
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                Video video = convert(resultSet);
                videos.add(video);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return videos;
    }

    @Override
    public Optional<Video> find(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM video WHERE id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Video video = convert(resultSet);
                return Optional.of(video);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

        return Optional.empty();
    }

    @Override
    public void insert(Video video) throws SQLException {

    }

    @Override
    public void update(long id, Video video) throws SQLException {

    }

    @Override
    public void delete(long id) throws SQLException {

    }

    @Override
    public List<Video> findAllByEpisodeId(int episodeId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Video> videos = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM video where episode_id=?")) {
            preparedStatement.setLong(1, episodeId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Video video = convert(resultSet);
                videos.add(video);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return videos;
    }

    @Override
    public List<Video> findAllByVideoId(int videoId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Video> videos = new ArrayList<>();
        try (PreparedStatement preparedStatement
                     = connection.prepareStatement("SELECT * FROM video where episode_id = " +
                "(select episode_id from video where id = ?)")) {
            preparedStatement.setLong(1, videoId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Video video = convert(resultSet);
                videos.add(video);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return videos;
    }


    @Override
    public Optional<Video> findMaxPriorityByEpisode(int episodeId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select * from video where episode_id = ? order by priority limit 1")) {
            preparedStatement.setLong(1, episodeId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Video video = convert(resultSet);
                return Optional.of(video);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

        return Optional.empty();
    }
}