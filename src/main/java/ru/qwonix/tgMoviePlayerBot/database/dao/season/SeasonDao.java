package ru.qwonix.tgMoviePlayerBot.database.dao.season;

import ru.qwonix.tgMoviePlayerBot.database.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.util.List;


public interface SeasonDao extends DefaultDao<Season> {

    List<Season> findAllBySeriesOrderByNumber(Series series) throws SQLException;
}
