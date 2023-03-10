package ru.qwonix.tgMoviePlayerBot.database.service.series;

import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.util.Optional;

public interface SeriesService {
    Optional<Series> find(int id);
}
