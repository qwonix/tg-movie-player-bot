package ru.qwonix.tgMoviePlayerBot.database.service.video;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Movie;
import ru.qwonix.tgMoviePlayerBot.entity.Video;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class VideoServiceImpl implements VideoService {
    private final VideoDao videoDao;

    public VideoServiceImpl(ConnectionPool connectionPool) {
        this.videoDao = new VideoDaoImpl(connectionPool);
    }


    @Override
    public Optional<Video> find(int id) {
        try {
            return videoDao.find(id);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Video> findMaxPriorityByEpisode(Episode episode) {
        try {
            return videoDao.findMaxPriorityByEpisodeId(episode.getId());
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Video> findMaxPriorityByMovie(Movie movie) {
        try {
            return videoDao.findMaxPriorityByMovieId(movie.getId());
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Video> findAllVideoByVideo(Video video) {
        try {
            return videoDao.findAllVideoByVideoId(video.getId());
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }
}
