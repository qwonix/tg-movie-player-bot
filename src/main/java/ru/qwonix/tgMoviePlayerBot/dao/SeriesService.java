package ru.qwonix.tgMoviePlayerBot.dao;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.config.DatabaseConfig;
import ru.qwonix.tgMoviePlayerBot.dao.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.dao.DaoException;
import ru.qwonix.tgMoviePlayerBot.dao.PoolConnectionBuilder;
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

    {
        try {
            connectionBuilder = new PoolConnectionBuilder(
                    DatabaseConfig.getProperty(DatabaseConfig.DB_URL),
                    DatabaseConfig.getProperty(DatabaseConfig.DB_USER),
                    DatabaseConfig.getProperty(DatabaseConfig.DB_PASSWORD),
                    10
            );
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
    }

    private final SeriesDao seriesDao = new SeriesDaoImpl(connectionBuilder);
    private final EpisodeDao episodeDao = new EpisodeDaoImpl(connectionBuilder);
    private final SeasonDao seasonDao = new SeasonDaoImpl(connectionBuilder);

    public boolean exists(Series series) {
        try {
            return seriesDao.find(series.getId()).isPresent();
        } catch (SQLException e) {
            log.error("sql exception {}", e.getMessage());
        }
        return false;
    }

    public Optional<Episode> findEpisode(int id) {
        try {
            return episodeDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception {}", e.getMessage());
        }
        return Optional.empty();
    }

    public List<Episode> findAllEpisodes() {
        try {
            return episodeDao.findAll();
        } catch (SQLException e) {
            log.error("sql exception {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<Episode> findAllEpisodesBySerial(Series series) {
        try {
            return episodeDao.findAll().stream()
                    .filter(episode -> episode.getSeason().getSeries().equals(series))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            log.error("sql exception {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<Series> findAll() {
        try {
            return seriesDao.findAll();
        } catch (SQLException e) {
            log.error("sql exception {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<Series> findAllByName(String name) {
        try {
            return seriesDao.findAllByName(name);
        } catch (SQLException e) {
            log.error("sql exception {}", e.getMessage());
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
            log.error("sql exception {}", e.getMessage());
        }
    }
}
