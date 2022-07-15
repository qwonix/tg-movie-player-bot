package ru.qwonix.tgMoviePlayerBot.dao;

import lombok.Data;
import ru.qwonix.tgMoviePlayerBot.config.DatabaseConfig;
import ru.qwonix.tgMoviePlayerBot.dao.user.UserService;

import java.sql.SQLException;

@Data
public class DaoContext {
    private final ConnectionBuilder connectionBuilder;
    private UserService userService;
    private SeriesService seriesService;

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

    public DaoContext() {
        userService = new UserService(connectionBuilder);
        seriesService = new SeriesService(connectionBuilder);
    }
}
