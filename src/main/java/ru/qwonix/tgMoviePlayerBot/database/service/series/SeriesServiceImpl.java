package ru.qwonix.tgMoviePlayerBot.database.service.series;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SeriesServiceImpl implements SeriesService {
    private final SeriesDao seriesDao;

    public SeriesServiceImpl(ConnectionBuilder connectionBuilder) {
        this.seriesDao = new SeriesDaoImpl(connectionBuilder);
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
    public Optional<Series> find(int id) {
        try {
            return seriesDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
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
    public List<Series> findAllWithLimitAndPage(int limit, int page) {
        try {
            return seriesDao.findAllWithLimitAndPage(limit, page);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Series> findAllByNameLikeWithLimitAndPage(String name, int limit, int page) {
        try {
            return seriesDao.findAllByNameLikeWithLimitAndPage(name, limit, page);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    @Override
    public int countAllByNameLike(String name) {
        try {
            return seriesDao.countAllByNameLike(name);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return -1;
    }
}
