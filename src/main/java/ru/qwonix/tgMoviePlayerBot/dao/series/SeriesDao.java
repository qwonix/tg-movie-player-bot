package ru.qwonix.tgMoviePlayerBot.dao.series;

import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SeriesDao {
    Series convert(ResultSet seriesResultSet) throws SQLException;

    List<Series> findAll() throws SQLException;

    List<Series> findAllByName(String name) throws SQLException;

    Optional<Series> find(long id) throws SQLException;

    void insert(Series series) throws SQLException;

    void update(long id, Series series) throws SQLException;

    void delete(long id) throws SQLException;
}
