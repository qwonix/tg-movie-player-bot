package ru.qwonix.tgMoviePlayerBot.database.dao.show;

import ru.qwonix.tgMoviePlayerBot.database.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShowDao extends DefaultDao<Show> {

    Optional<Show> findBySeries(long id) throws SQLException;
}
