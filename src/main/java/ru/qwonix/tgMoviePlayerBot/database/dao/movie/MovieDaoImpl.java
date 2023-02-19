package ru.qwonix.tgMoviePlayerBot.database.dao.movie;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.show.ShowDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.show.ShowDaoImpl;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Movie;
import ru.qwonix.tgMoviePlayerBot.entity.Show;
import ru.qwonix.tgMoviePlayerBot.entity.Video;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovieDaoImpl implements MovieDao {
    private final ConnectionBuilder connectionBuilder;

    public MovieDaoImpl(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    @Override
    public Movie convert(ResultSet resultSet) throws SQLException {
        ShowDao showDao = new ShowDaoImpl(connectionBuilder);
        VideoDao videoDao = new VideoDaoImpl(connectionBuilder);
        Optional<Show> show = showDao.find(resultSet.getInt("show_id"));
        Optional<Video> video = videoDao.find(resultSet.getInt("video_id"));

        return Movie.builder()
                .id(resultSet.getInt("id"))
                .title(resultSet.getString("title"))
                .description(resultSet.getString("description"))
                .previewTgFileId(resultSet.getString("preview_tg_file_id"))
                .video(video.orElse(null))
                .show(show.orElse(null))
                .build();
    }


    @Override
    public List<Movie> findAll() throws SQLException {
        Connection connection = connectionBuilder.getConnection();
        List<Movie> serials = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String SQL = "SELECT * FROM movie";
            ResultSet resultSet = statement.executeQuery(SQL);

            while (resultSet.next()) {
                Movie movie = convert(resultSet);
                serials.add(movie);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return serials;
    }


    @Override
    public Optional<Movie> find(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM movie WHERE id = ?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Movie movie = convert(resultSet);
                return Optional.of(movie);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

        return Optional.empty();
    }

    @Override
    public void insert(Movie movie) throws SQLException {

    }

    @Override
    public void update(long id, Movie movie) throws SQLException {

    }

    @Override
    public void delete(long id) throws SQLException {

    }

    @Override
    public List<Movie> findByShowId(int showId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Movie> movies = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM movie where show_id = ?")) {
            preparedStatement.setLong(1, showId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Movie movie = convert(resultSet);
                movies.add(movie);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return movies;
    }
}