package ru.qwonix.tgMoviePlayerBot.database.service.user;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.database.ConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.dao.user.UserDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.user.UserDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.sql.SQLException;
import java.util.Optional;


@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(ConnectionPool connectionPool) {
        userDao = new UserDaoImpl(connectionPool);
    }


    @Override
    public User makeAdmin(User user) {
        User admin = user.toBuilder().isAdmin(true).build();
        merge(admin);
        return admin;
    }

    @Override
    public boolean exists(long chatId) {
        try {
            return userDao.find(chatId).isPresent();
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
        return false;
    }

    @Override
    public void merge(User user) {
        try {
            if (exists(user.getChatId())) {
                userDao.update(user.getChatId(), user);
            } else {
                userDao.insert(user);
            }
        } catch (SQLException e) {
            log.error("sql exception", e);
        }
    }

    @Override
    public Optional<User> findUser(long userChatId) {
        try {
            return userDao.find(userChatId);
        } catch (SQLException e) {
            log.error("sql exception", e);
        }

        return Optional.empty();
    }
}
