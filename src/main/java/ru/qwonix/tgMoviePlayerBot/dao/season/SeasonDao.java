package ru.qwonix.tgMoviePlayerBot.dao.season;

import ru.qwonix.tgMoviePlayerBot.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.util.List;


public interface SeasonDao extends DefaultDao<Season> {

    List<Season> findAllBySeries(Series series) throws SQLException;
}
