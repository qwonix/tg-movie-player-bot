package ru.qwonix.tgMoviePlayerBot.database.service.season;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.season.SeasonDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.season.SeasonDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SeasonServiceImpl implements SeasonService {
    private final SeasonDao seasonDao;

    public SeasonServiceImpl(ConnectionBuilder connectionBuilder) {
        this.seasonDao = new SeasonDaoImpl(connectionBuilder);
    }

    @Override
    public Optional<Season> find(int id) {
        try {
            return seasonDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Season> findAllBySeriesOrderByNumber(Series series) {
        try {
            return seasonDao.findAllBySeriesOrderByNumber(series);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Season> findAllBySeriesOrderByNumberWithLimitAndPage(Series series, int limit, int page) {
        try {
            return seasonDao.findAllBySeriesOrderByNumberWithLimitAndPage(series, limit, page);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }

    @Override
    public int countAllBySeries(Series series) {
        try {
            return seasonDao.countAllBySeries(series);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return -1;
    }
}
