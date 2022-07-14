package ru.qwonix.tgMoviePlayerBot.dao.season;

import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SeasonDao {
    Season convert(ResultSet seasonResultSet) throws SQLException;

    List<Season> findAll() throws SQLException;

    List<Season> findAllBySeries(Series series) throws SQLException;

    Optional<Season> find(long id) throws SQLException;

    void insert(Season season) throws SQLException;

    void update(long id, Season season) throws SQLException;

    void delete(long id) throws SQLException;
}
