package ru.qwonix.tgMoviePlayerBot.database.dao.video;

import ru.qwonix.tgMoviePlayerBot.database.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.Video;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface VideoDao extends DefaultDao<Video> {
    Video convert(ResultSet userResultSet) throws SQLException;

    List<Video> findAll() throws SQLException;

    List<Video> findAllByEpisodeId(int episodeId) throws SQLException;

    List<Video> findAllByMovieId(int episodeId) throws SQLException;

    Optional<Video> findMaxPriorityByEpisodeId(int episodeId) throws SQLException;

    Optional<Video> findMaxPriorityByMovieId(int episodeId) throws SQLException;

    List<Video> findAllVideoByVideoId(int videoId) throws SQLException;
}