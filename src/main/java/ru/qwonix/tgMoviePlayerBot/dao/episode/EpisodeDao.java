package ru.qwonix.tgMoviePlayerBot.dao.episode;

import ru.qwonix.tgMoviePlayerBot.entity.Episode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EpisodeDao {

    Episode convert(ResultSet episodeResultSet) throws SQLException;

    List<Episode> findAll() throws SQLException;

    Optional<Episode> find(int id) throws SQLException;

    void insert(Episode episode) throws SQLException;
}