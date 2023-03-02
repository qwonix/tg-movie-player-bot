package ru.qwonix.tgMoviePlayerBot.database.service.series;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.series.SeriesDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.sql.SQLException;
import java.time.LocalDate;
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
    public Optional<Series> find(int id) {
        try {
            return seriesDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }
}
