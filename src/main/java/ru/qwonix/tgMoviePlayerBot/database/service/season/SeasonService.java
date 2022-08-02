package ru.qwonix.tgMoviePlayerBot.database.service.season;

import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.util.List;
import java.util.Optional;

public interface SeasonService {
    Optional<Season> findSeason(int id);

    List<Season> findSeasonsBySeriesOrderByNumber(Series series);
}
