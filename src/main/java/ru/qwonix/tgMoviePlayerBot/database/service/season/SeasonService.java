package ru.qwonix.tgMoviePlayerBot.database.service.season;

import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.util.List;
import java.util.Optional;

public interface SeasonService {
    Optional<Season> find(int id);

    List<Season> findAllBySeriesOrderByNumberWithLimitAndPage(Series series, int limit, int page);

    int countAllBySeries(Series series);

}
