package ru.qwonix.tgMoviePlayerBot.database.dao.movie;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.dao.show.ShowDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.show.ShowDaoImpl;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Movie;
import ru.qwonix.tgMoviePlayerBot.entity.Show;
import ru.qwonix.tgMoviePlayerBot.entity.Video;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovieDaoImpl implements MovieDao {
    private final ConnectionPool connectionPool;

    public MovieDaoImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Movie convert(ResultSet resultSet) throws SQLException {
        ShowDao showDao = new ShowDaoImpl(connectionPool);
        VideoDao videoDao = new VideoDaoImpl(connectionPool);
        Optional<Show> show = showDao.find(resultSet.getInt("show_id"));
        List<Video> videos = videoDao.findAllByMovieId(resultSet.getInt("id"));

        return Movie.builder()
                .id(resultSet.getInt("id"))
                .title(resultSet.getString("title"))
                .description(resultSet.getString("description"))
                .previewTgFileId(resultSet.getString("preview_tg_file_id"))
                .videos(videos)
                .show(show.orElse(null))
                .build();
    }


    @Override
    public Optional<Movie> find(long id) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM movie WHERE id = ?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Movie movie = convert(resultSet);
                return Optional.of(movie);
            }
        } finally {
            connectionPool.releaseConnection(connection);
        }

        return Optional.empty();
    }


    @Override
    public List<Movie> findByShowId(int showId) throws SQLException {
        Connection connection = connectionPool.getConnection();

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
            connectionPool.releaseConnection(connection);
        }
        return movies;
    }
}