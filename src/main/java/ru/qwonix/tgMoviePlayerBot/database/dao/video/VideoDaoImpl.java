package ru.qwonix.tgMoviePlayerBot.database.dao.video;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.entity.Video;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class VideoDaoImpl implements VideoDao {
    private final ConnectionBuilder connectionBuilder;

    public VideoDaoImpl(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    @Override
    public Video convert(ResultSet resultSet) throws SQLException {
        return Video.builder()
                .id(resultSet.getInt("id"))
                .videoTgFileId(resultSet.getString("video_tg_file_id"))
                .resolution(resultSet.getInt("resolution"))
                .audioLanguage(resultSet.getString("audio_language"))
                .subtitlesLanguage(resultSet.getString("subtitles_language"))
                .priority(resultSet.getInt("priority"))
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


    private List<Video> findAllByEntityIdAndEntityType(int entityId, String entityType) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Video> videos = new ArrayList<>();
        try (PreparedStatement preparedStatement
                     = connection.prepareStatement("select * from video " +
                "inner join video_entity ve on video.id = ve.video_id " +
                "where entity_id = ? and entity_type=?")) {
            preparedStatement.setLong(1, entityId);
            preparedStatement.setString(2, entityType);

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
    public List<Video> findAllByEpisodeId(int episodeId) throws SQLException {
        return findAllByEntityIdAndEntityType(episodeId, "episode");
    }

    @Override
    public List<Video> findAllByMovieId(int movieId) throws SQLException {
        return findAllByEntityIdAndEntityType(movieId, "movie");
    }


    @Override
    public Optional<Video> findMaxPriorityByEpisodeId(int episodeId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select * from video " +
                             "inner join video_entity ve on video.id = ve.video_id " +
                             "where entity_id = ? and entity_type='episode' order by priority limit 1")) {
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

    @Override
    public Optional<Video> findMaxPriorityByMovieId(int movieId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select * from video " +
                             "inner join video_entity ve on video.id = ve.video_id " +
                             "where entity_id = ? and entity_type='movie' order by priority limit 1")) {
            preparedStatement.setLong(1, movieId);

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
    public List<Video> findAllVideoByVideoId(int videoId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement
                     = connection.prepareStatement("select entity_id, entity_type from video_entity where video_id = ?")) {
            preparedStatement.setLong(1, videoId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int entityId = resultSet.getInt("entity_id");
                String entityType = resultSet.getString("entity_type");

                return findAllByEntityIdAndEntityType(entityId, entityType);
            }
            return Collections.emptyList();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }
}