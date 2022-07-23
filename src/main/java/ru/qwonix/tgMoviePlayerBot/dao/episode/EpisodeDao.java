package ru.qwonix.tgMoviePlayerBot.dao.episode;

import ru.qwonix.tgMoviePlayerBot.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.util.List;

public interface EpisodeDao extends DefaultDao<Episode> {
    List<Episode> findAllBySeason(Season season) throws SQLException;

}