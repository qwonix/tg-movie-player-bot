package ru.qwonix.tgMoviePlayerBot.dao.user;


import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDao {

    User convert(ResultSet userResultSet) throws SQLException;

    List<User> findAll() throws SQLException;

    Optional<User> find(long chatId) throws SQLException;

    void insert(User user) throws SQLException;

    void update(long chatId, User user) throws SQLException;

    void delete(long id) throws SQLException;

}
