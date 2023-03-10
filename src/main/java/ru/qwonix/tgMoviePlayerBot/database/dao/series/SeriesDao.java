package ru.qwonix.tgMoviePlayerBot.database.dao.series;

import ru.qwonix.tgMoviePlayerBot.database.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.time.LocalDate;

public interface SeriesDao extends DefaultDao<Series> {
    LocalDate findPremiereReleaseDate(int seriesId) throws SQLException;
}
