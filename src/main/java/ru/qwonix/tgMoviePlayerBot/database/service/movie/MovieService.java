package ru.qwonix.tgMoviePlayerBot.database.service.movie;

import ru.qwonix.tgMoviePlayerBot.entity.Movie;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.util.List;
import java.util.Optional;

public interface MovieService {

    boolean exists(Movie movie);

    Optional<Movie> find(int id);

    List<Movie> findByShow(Show show);

}
