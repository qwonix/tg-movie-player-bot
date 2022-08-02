package ru.qwonix.tgMoviePlayerBot.database.servie;

import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SeriesService {
    boolean exists(Series series);

    Optional<Episode> findEpisode(int id);

    Optional<Series> findSeries(int id);

    List<Episode> findAllEpisodes();

    LocalDate findEpisodePremiereReleaseDate(Series series);

    List<Episode> findAllBySeasonOrderByNumber(Season season);

    Optional<Season> findSeason(int id);

    List<Season> findSeasonsBySeriesOrderByNumber(Series series);

    List<Series> findAll();

    List<Series> findAllByName(String name);

    List<Series> findAllByNameLike(String name);

    List<Series> findAllByNameLikeWithLimitAndOffset(String name, int limit, int offset);

    int countAllByNameLike(String name);

    void addOrUpdate(Series series);
}
