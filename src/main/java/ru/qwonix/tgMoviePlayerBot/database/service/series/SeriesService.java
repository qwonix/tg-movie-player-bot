package ru.qwonix.tgMoviePlayerBot.database.service.series;

import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SeriesService {

    boolean exists(Series series);

    Optional<Series> find(int id);

    List<Series> findByShow(Show show);

    LocalDate findPremiereReleaseDate(Series series);

    List<Series> findAllWithLimitAndPage(int limit, int page);

    List<Series> findAllByNameLikeWithLimitAndPage(String name, int limit, int page);

    int countAllByNameLike(String name);
}
