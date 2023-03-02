package ru.qwonix.tgMoviePlayerBot.database.dao.user;

import ru.qwonix.tgMoviePlayerBot.database.dao.DefaultDao;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.sql.SQLException;

public interface UserDao extends DefaultDao<User> {
    void insert(User user) throws SQLException;

    void update(long id, User user) throws SQLException;

}
