package ru.qwonix.tgMoviePlayerBot.database.service.series;

import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.util.List;
import java.util.Optional;

public interface SeriesService {

    boolean exists(Series series);

    Optional<Series> find(int id);

    List<Series> findAll();

    List<Series> findAllByName(String name);

    List<Series> findAllByNameLike(String name);

    List<Series> findAllByNameLikeWithLimitAndPage(String name, int limit, int page);

    int countAllByNameLike(String name);

    void addOrUpdate(Series series);
}
