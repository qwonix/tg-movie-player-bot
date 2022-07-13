package ru.qwonix.tgMoviePlayerBot.User;

import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;


@Slf4j
public class UserService {
    private final UserDao userDao = new UserDao();

    public User setAdmin(User user) {
        User admin = user.toBuilder().isAdmin(true).build();
        merge(admin);
        return admin;
    }

    public boolean exists(User user) {
        try {
            return userDao.find(user.getChatId()).isPresent();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void merge(User user) {
        try {
            if (exists(user)) {
                userDao.update(user.getChatId(), user);
            } else {
                userDao.save(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
