package ru.qwonix.tgMoviePlayerBot.database.service.series;

import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SeriesService {
    Optional<Series> find(int id);
}
