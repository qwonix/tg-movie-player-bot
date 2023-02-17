package ru.qwonix.tgMoviePlayerBot.database.service.video;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.video.VideoDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Video;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class VideoServiceImpl implements VideoService {
    private final VideoDao videoDao;

    public VideoServiceImpl(ConnectionBuilder connectionBuilder) {
        this.videoDao = new VideoDaoImpl(connectionBuilder);
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
    public List<Video> findAll() {
        try {
            return videoDao.findAll();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Collections.emptyList();
    }


    @Override
    public Optional<Video> findMaxPriorityByEpisode(Episode episode) {
        try {
            return videoDao.findMaxPriorityByEpisode(episode.getId());
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return Optional.empty();
    }
}
