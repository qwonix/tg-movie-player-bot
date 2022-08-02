package ru.qwonix.tgMoviePlayerBot.database.dao.episode;

import ru.qwonix.tgMoviePlayerBot.database.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface EpisodeDao extends DefaultDao<Episode> {
    List<Episode> findAllBySeasonOrderByNumber(Season season) throws SQLException;

    LocalDate findEpisodePremiereReleaseDate(Series series) throws SQLException;

    List<Episode> findAllBySeasonOrderByNumberWithLimitAndPage(Season season, int limit, int page) throws SQLException;

    int countAllBySeason(Season season) throws SQLException;
}