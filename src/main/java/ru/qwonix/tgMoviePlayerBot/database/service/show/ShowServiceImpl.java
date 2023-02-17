package ru.qwonix.tgMoviePlayerBot.database.service.show;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.dao.show.ShowDao;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ShowServiceImpl implements ShowService {
    private final ShowDao showDao;

    public ShowServiceImpl(ShowDao showDao) {
        this.showDao = showDao;
    }

    @Override
    public boolean exists(Show show) {
        try {
            return showDao.find(show.getId()).isPresent();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return false;
    }

    @Override
    public Optional<Show> find(int id) {
        try {
            return showDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }


    @Override
    public Optional<Show> findBySeries(Series series) {
        try {
            return showDao.findBySeries(series.getId());
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Show> findAll() {
        try {
            return showDao.findAll();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }
}
