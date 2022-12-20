package ru.qwonix.tgMoviePlayerBot.database.service.episode;

import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;

import java.util.List;
import java.util.Optional;

public interface EpisodeService {
    Optional<Episode> find(int id);

    Optional<Episode> findNext(Episode episode);

    Optional<Episode> findPrevious(Episode episode);


    Optional<Episode> findLast(Season season);

    List<Episode> findAll();

    List<Episode> findAllBySeasonOrderByNumber(Season season);

    List<Episode> findAllBySeasonOrderByNumberWithLimitAndPage(Season season, int limit, int page);

    int countAllBySeason(Season season);

    void insertOrUpdate(Episode episode);
}
