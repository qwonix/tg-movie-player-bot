package ru.qwonix.tgMoviePlayerBot.database.service.show;

import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShowService {

    boolean exists(Show show);

    Optional<Show> find(int id);

    Optional<Show> findBySeries(Series series);

    List<Show> findAll();
}
