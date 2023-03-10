package ru.qwonix.tgMoviePlayerBot.database.service.series;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.util.Optional;

@Slf4j
public class SeriesServiceImpl implements SeriesService {
    private final SeriesDao seriesDao;

    public SeriesServiceImpl(ConnectionPool connectionPool) {
        this.seriesDao = new SeriesDaoImpl(connectionPool);
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
}
