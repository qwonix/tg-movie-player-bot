package ru.qwonix.tgMoviePlayerBot.database.service.series;

import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SeriesService {

    boolean exists(Series series);

    Optional<Series> findSeries(int id);

    List<Series> findAll();

    List<Series> findAllByName(String name);

    List<Series> findAllByNameLike(String name);

    List<Series> findAllByNameLikeWithLimitAndOffset(String name, int limit, int offset);

    int countAllByNameLike(String name);

    void addOrUpdate(Series series);
}
