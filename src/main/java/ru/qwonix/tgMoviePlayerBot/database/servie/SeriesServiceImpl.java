package ru.qwonix.tgMoviePlayerBot.database.servie;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.episode.EpisodeDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.episode.EpisodeDaoImpl;
import ru.qwonix.tgMoviePlayerBot.database.dao.season.SeasonDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.season.SeasonDaoImpl;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SeriesServiceImpl implements SeriesService {
    private final SeriesDao seriesDao;
    private final EpisodeDao episodeDao;
    private final SeasonDao seasonDao;

    public SeriesServiceImpl(ConnectionBuilder connectionBuilder) {
        this.seriesDao = new SeriesDaoImpl(connectionBuilder);
        this.episodeDao = new EpisodeDaoImpl(connectionBuilder);
        this.seasonDao = new SeasonDaoImpl(connectionBuilder);
    }

    @Override
    public boolean exists(Series series) {
        try {
            return seriesDao.find(series.getId()).isPresent();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return false;
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
    public Optional<Series> findSeries(int id) {
        try {
            return seriesDao.find(id);
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
    public List<Episode> findAllEpisodesBySeason(Season season) {
        try {
            return episodeDao.findAllBySeason(season);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }
    @Override
    public Optional<Season> findSeason(int id) {
        try {
            return seasonDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }
    @Override
    public List<Season> findSeasonsBySeries(Series series) {
        try {
            return seasonDao.findAllBySeries(series);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }
    @Override
    public List<Series> findAll() {
        try {
            return seriesDao.findAll();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }
    @Override
    public List<Series> findAllByName(String name) {
        try {
            return seriesDao.findAllByName(name);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }
    @Override
    public List<Series> findAllByNameLike(String name) {
        try {
            return seriesDao.findAllByNameLike(name);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }
    @Override
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
