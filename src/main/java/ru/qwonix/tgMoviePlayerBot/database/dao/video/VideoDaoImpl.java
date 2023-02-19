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

    @Override
    public List<Video> findAllByEpisodeId(int episodeId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Video> videos = new ArrayList<>();
//        try (PreparedStatement preparedStatement
//                     = connection.prepareStatement("SELECT * FROM video where video_tg_file_id in " +
//                "(select video_tg_file_id from episode_video where episode_id = ?)")) {
        try (PreparedStatement preparedStatement
                     = connection.prepareStatement("select * from video " +
                "inner join episode_video ev on video.id = ev.video_id " +
                "where episode_id = ?")) {
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
    public Optional<Video> findMaxPriorityByEpisode(int episodeId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select * from video " +
                             "inner join episode_video ev on video.id = ev.video_id " +
                             "where episode_id = ? order by priority limit 1")) {
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
    public List<Video> findAllVideoByVideoId(int videoId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Video> videos = new ArrayList<>();
        try (PreparedStatement preparedStatement
                     = connection.prepareStatement("select * from video where id in " +
                "(select video_id from episode_video where episode_id = (select episode_id from episode_video where video_id = ?))")) {
            preparedStatement.setLong(1, videoId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Video video = convert(resultSet);
                videos.add(video);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

        if (videos.isEmpty()) {
            try (PreparedStatement preparedStatement
                         = connection.prepareStatement("select * from video where id = ?")) {
                preparedStatement.setLong(1, videoId);

                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    Video video = convert(resultSet);
                    videos.add(video);
                }
            } finally {
                connectionBuilder.releaseConnection(connection);
            }
        }

        return videos;
    }
}