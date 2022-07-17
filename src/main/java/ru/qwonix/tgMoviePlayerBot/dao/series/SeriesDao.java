package ru.qwonix.tgMoviePlayerBot.dao.series;

import ru.qwonix.tgMoviePlayerBot.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.util.List;

public interface SeriesDao extends DefaultDao<Series> {

    List<Series> findAllByName(String name) throws SQLException;

    List<Series> findAllByNameLike(String name) throws SQLException;
}
