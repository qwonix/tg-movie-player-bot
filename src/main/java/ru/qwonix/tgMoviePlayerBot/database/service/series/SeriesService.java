package ru.qwonix.tgMoviePlayerBot.database.service.series;

import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.util.List;
import java.util.Optional;

public interface SeriesService {

    boolean exists(Series series);

    Optional<Series> find(int id);

    List<Series> findAll();

    List<Series> findAllWithLimitAndPage(int limit, int page);

    List<Series> findAllByNameLikeWithLimitAndPage(String name, int limit, int page);

    int countAllByNameLike(String name);
}
