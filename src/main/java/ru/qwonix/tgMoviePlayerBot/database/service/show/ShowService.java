package ru.qwonix.tgMoviePlayerBot.database.service.show;

import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.util.Optional;

public interface ShowService {
    Optional<Show> find(int id);
}
