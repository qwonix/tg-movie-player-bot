package ru.qwonix.tgMoviePlayerBot.database.service.show;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.dao.show.ShowDao;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.sql.SQLException;
import java.util.Optional;

@Slf4j
public class ShowServiceImpl implements ShowService {
    private final ShowDao showDao;

    public ShowServiceImpl(ShowDao showDao) {
        this.showDao = showDao;
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
}
