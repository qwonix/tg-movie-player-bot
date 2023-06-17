package ru.qwonix.tgMoviePlayerBot.database.dao.episode;

import ru.qwonix.tgMoviePlayerBot.database.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EpisodeDao extends DefaultDao<Episode> {

    Optional<Episode> findNext(long episodeId) throws SQLException;

    Optional<Episode> findPrevious(long episodeId, long seasonId) throws SQLException;

    List<Episode> findAllBySeasonOrderByNumberWithLimitAndPage(long seasonId, int limit, int page) throws SQLException;

    int countAllBySeasonId(long seasonId) throws SQLException;

    void setAvailableByEpisodeProductionCode(int episodeProductionCode, Boolean isAvailable) throws SQLException;
}