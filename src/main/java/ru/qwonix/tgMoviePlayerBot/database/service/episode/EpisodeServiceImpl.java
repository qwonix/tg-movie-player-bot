package ru.qwonix.tgMoviePlayerBot.database.service.episode;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.episode.EpisodeDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.episode.EpisodeDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class EpisodeServiceImpl implements EpisodeService {
    private final EpisodeDao episodeDao;

    public EpisodeServiceImpl(ConnectionBuilder connectionBuilder) {
        this.episodeDao = new EpisodeDaoImpl(connectionBuilder);
    }

    @Override
    public Optional<Episode> findEpisode(int id) {
        try {
            return episodeDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Episode> findAllEpisodes() {
        try {
            return episodeDao.findAll();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    @Override
    public LocalDate findEpisodePremiereReleaseDate(Series series) {
        try {
            return episodeDao.findEpisodePremiereReleaseDate(series);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return null;
    }

    @Override
    public List<Episode> findAllBySeasonOrderByNumber(Season season) {
        try {
            return episodeDao.findAllBySeasonOrderByNumber(season);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Episode> findAllBySeasonOrderByNumberWithLimitAndOffset(Season season, int limit, int offset) {
        try {
            return episodeDao.findAllBySeasonOrderByNumberWithLimitAndOffset(season, limit, offset);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }
}
