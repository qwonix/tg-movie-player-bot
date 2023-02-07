package ru.qwonix.tgMoviePlayerBot.database.service.video;

import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Video;

import java.util.List;
import java.util.Optional;

public interface VideoService {
    List<Video> findAll();

    Optional<Video> find(int id);

    Optional<Video> findMaxPriorityByEpisode(Episode episode);

    List<Video> findAllByVideo(Video video);

}
