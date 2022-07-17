package ru.qwonix.tgMoviePlayerBot.dao;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.dao.episode.EpisodeDao;
import ru.qwonix.tgMoviePlayerBot.dao.episode.EpisodeDaoImpl;
import ru.qwonix.tgMoviePlayerBot.dao.season.SeasonDao;
import ru.qwonix.tgMoviePlayerBot.dao.season.SeasonDaoImpl;
import ru.qwonix.tgMoviePlayerBot.dao.series.SeriesDao;
import ru.qwonix.tgMoviePlayerBot.dao.series.SeriesDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class SeriesService {
    private final ConnectionBuilder connectionBuilder;

    private final SeriesDao seriesDao;
    private final EpisodeDao episodeDao;
    private final SeasonDao seasonDao;

    public SeriesService(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
        seriesDao = new SeriesDaoImpl(connectionBuilder);
        episodeDao = new EpisodeDaoImpl(connectionBuilder);
        seasonDao = new SeasonDaoImpl(connectionBuilder);
    }

    public boolean exists(Series series) {
        try {
            return seriesDao.find(series.getId()).isPresent();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return false;
    }

    public Optional<Episode> findEpisode(int id) {
        try {
            return episodeDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }

    public List<Episode> findAllEpisodes() {
        try {
            return episodeDao.findAll();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    public List<Episode> findAllEpisodesBySerial(Series series) {
        try {
            // TODO: 17-Jul-22 change filter to sql select exp
            return episodeDao.findAll().stream()
                    .filter(episode -> episode.getSeason().getSeries().getId() == series.getId())
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    public List<Series> findAll() {
        try {
            return seriesDao.findAll();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    public List<Series> findAllByName(String name) {
        try {
            return seriesDao.findAllByName(name);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    public List<Series> findAllByNameLike(String name) {
        try {
            return seriesDao.findAllByNameLike(name);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    public void addOrUpdate(Series series) {
        try {
            if (exists(series)) {
                seriesDao.update(series.getId(), series);
            } else {
                seriesDao.insert(series);
            }
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
    }
}
