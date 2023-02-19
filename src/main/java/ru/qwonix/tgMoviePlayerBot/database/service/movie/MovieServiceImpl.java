package ru.qwonix.tgMoviePlayerBot.database.service.movie;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.movie.MovieDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.movie.MovieDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Movie;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MovieServiceImpl implements MovieService {
    private final MovieDao movieDao;

    public MovieServiceImpl(ConnectionBuilder connectionBuilder) {
        this.movieDao = new MovieDaoImpl(connectionBuilder);
    }

    @Override
    public boolean exists(Movie movie) {
        try {
            return movieDao.find(movie.getId()).isPresent();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return false;
    }

    @Override
    public Optional<Movie> find(int id) {
        try {
            return movieDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Movie> findByShow(Show show) {
        try {
            return movieDao.findByShowId(show.getId());
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }
}
