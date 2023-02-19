package ru.qwonix.tgMoviePlayerBot.database.dao.movie;

import ru.qwonix.tgMoviePlayerBot.database.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Movie;

import java.sql.SQLException;
import java.util.List;

public interface MovieDao extends DefaultDao<Movie> {

    List<Movie> findByShowId(int showId) throws SQLException;
}
