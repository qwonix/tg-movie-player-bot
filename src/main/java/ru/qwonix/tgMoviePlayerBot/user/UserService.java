package ru.qwonix.tgMoviePlayerBot.user;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.config.DatabaseConfig;
import ru.qwonix.tgMoviePlayerBot.dao.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.dao.DaoException;
import ru.qwonix.tgMoviePlayerBot.dao.PoolConnectionBuilder;

import java.sql.SQLException;


@Slf4j
public class UserService {
    private final ConnectionBuilder connectionBuilder;
    {
        try {
            connectionBuilder = new PoolConnectionBuilder(
                    DatabaseConfig.getProperty(DatabaseConfig.DB_URL),
                    DatabaseConfig.getProperty(DatabaseConfig.DB_USER),
                    DatabaseConfig.getProperty(DatabaseConfig.DB_PASSWORD),
                    10
            );
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
    }

    private final UserDao userDao = new UserDao(connectionBuilder);

    public User setAdmin(User user) {
        User admin = user.toBuilder().isAdmin(true).build();
        merge(admin);
        return admin;
    }

    public boolean exists(User user) {
        try {
            return userDao.find(user.getChatId()).isPresent();
        } catch (SQLException e) {
            log.error("sql exception {}", e.getMessage());
        }
        return false;
    }

    public void merge(User user) {
        try {
            if (exists(user)) {
                userDao.update(user.getChatId(), user);
            } else {
                userDao.insert(user);
            }
        } catch (SQLException e) {
            log.error("sql exception {}", e.getMessage());
        }
    }
}
