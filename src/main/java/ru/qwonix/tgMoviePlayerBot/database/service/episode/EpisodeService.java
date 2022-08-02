package ru.qwonix.tgMoviePlayerBot.database.service.episode;

import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EpisodeService {
    Optional<Episode> find(int id);

    List<Episode> findAll();

    LocalDate findPremiereReleaseDate(Series series);

    List<Episode> findAllBySeasonOrderByNumber(Season season);

    List<Episode> findAllBySeasonOrderByNumberWithLimitAndPage(Season season, int limit, int page);
}
