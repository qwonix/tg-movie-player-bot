package ru.qwonix.tgMoviePlayerBot.database.dao.series;

import ru.qwonix.tgMoviePlayerBot.database.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.util.List;

public interface SeriesDao extends DefaultDao<Series> {

    int countAllByNameLike(String name) throws SQLException;

    List<Series> findAllByNameLikeWithLimitAndPage(String name, int limit, int page) throws SQLException;

    List<Series> findAllWithLimitAndPage(int limit, int page) throws SQLException;

}
