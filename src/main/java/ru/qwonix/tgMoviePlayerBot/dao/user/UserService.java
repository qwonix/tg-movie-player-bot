package ru.qwonix.tgMoviePlayerBot.dao.user;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.dao.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.sql.SQLException;


@Slf4j
public class UserService {
    private final ConnectionBuilder connectionBuilder;

    private final UserDao userDao;

    public UserService(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
        userDao = new UserDaoImpl(connectionBuilder);
    }

    public User setAdmin(User user) {
        User admin = user.toBuilder().isAdmin(true).build();
        merge(admin);
        return admin;
    }

    public boolean exists(long chatId) {
        try {
            return userDao.find(chatId).isPresent();
        } catch (SQLException e) {
            log.error("sql exception {}", e.getMessage());
        }
        return false;
    }

    public void merge(User user) {
        try {
            if (exists(user.getChatId())) {
                userDao.update(user.getChatId(), user);
            } else {
                userDao.insert(user);
            }
        } catch (SQLException e) {
            log.error("sql exception {}", e.getMessage());
        }
    }
}
