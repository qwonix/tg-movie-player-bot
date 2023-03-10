package ru.qwonix.tgMoviePlayerBot.database.service.episode;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.dao.episode.EpisodeDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.episode.EpisodeDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class EpisodeServiceImpl implements EpisodeService {
    private final EpisodeDao episodeDao;

    public EpisodeServiceImpl(ConnectionPool connectionPool) {
        this.episodeDao = new EpisodeDaoImpl(connectionPool);
    }

    @Override
    public Optional<Episode> find(int id) {
        try {
            return episodeDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }


    @Override
    public Optional<Episode> findNext(Episode episode) {
        try {
            return episodeDao.findNext(episode.getId());
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Episode> findPrevious(Episode episode) {
        try {
            return episodeDao.findPrevious(episode.getId(), episode.getSeason().getId());
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Episode> findAllBySeasonOrderByNumberWithLimitAndPage(Season season, int limit, int page) {
        try {
            return episodeDao.findAllBySeasonOrderByNumberWithLimitAndPage(season.getId(), limit, page);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    @Override
    public int countAllBySeason(Season season) {
        try {
            return episodeDao.countAllBySeasonId(season.getId());
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return -1;
    }
}
