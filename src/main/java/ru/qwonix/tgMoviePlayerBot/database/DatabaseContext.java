package ru.qwonix.tgMoviePlayerBot.database;

import lombok.Data;
import ru.qwonix.tgMoviePlayerBot.config.DatabaseConfig;
import ru.qwonix.tgMoviePlayerBot.database.dao.DaoException;
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeService;
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.movie.MovieService;
import ru.qwonix.tgMoviePlayerBot.database.service.movie.MovieServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonService;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesService;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserService;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.video.VideoService;
import ru.qwonix.tgMoviePlayerBot.database.service.video.VideoServiceImpl;

import java.sql.SQLException;

@Data
public class DatabaseContext {
    private final ConnectionBuilder connectionBuilder;

    private final UserService userService;
    private final SeriesService seriesService;
    private final MovieService movieService;
    private final SeasonService seasonService;
    private final EpisodeService episodeService;
    private final VideoService videoService;

    {
        try {
            connectionBuilder = new PoolConnectionBuilder(
                    DatabaseConfig.getProperty(DatabaseConfig.DB_URL),
                    DatabaseConfig.getProperty(DatabaseConfig.DB_USER),
                    DatabaseConfig.getProperty(DatabaseConfig.DB_PASSWORD),
                    10
            );
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    connectionBuilder.closeConnections();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
    }

    public DatabaseContext() {
        userService = new UserServiceImpl(connectionBuilder);
        seriesService = new SeriesServiceImpl(connectionBuilder);
        movieService = new MovieServiceImpl(connectionBuilder);
        seasonService = new SeasonServiceImpl(connectionBuilder);
        episodeService = new EpisodeServiceImpl(connectionBuilder);
        videoService = new VideoServiceImpl(connectionBuilder) {
        };
    }
}
