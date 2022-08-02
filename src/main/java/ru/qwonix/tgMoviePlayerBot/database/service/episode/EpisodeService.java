package ru.qwonix.tgMoviePlayerBot.database.service.episode;

import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EpisodeService {
    Optional<Episode> findEpisode(int id);

    List<Episode> findAllEpisodes();

    LocalDate findEpisodePremiereReleaseDate(Series series);

    List<Episode> findAllBySeasonOrderByNumber(Season season);

    List<Episode> findAllBySeasonOrderByNumberWithLimitAndOffset(Season season, int limit, int offset);
}
