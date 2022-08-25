package ru.qwonix.tgMoviePlayerBot.database.dao.season;

import ru.qwonix.tgMoviePlayerBot.database.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;


public interface SeasonDao extends DefaultDao<Season> {

    LocalDate findPremiereReleaseDate(int seasonId) throws SQLException;

    LocalDate findFinalReleaseDate(int seasonId) throws SQLException;

    List<Season> findAllBySeriesOrderByNumberWithLimitAndPage(Series series, int limit, int page) throws SQLException;

    int countAllBySeries(Series series) throws SQLException;
}
