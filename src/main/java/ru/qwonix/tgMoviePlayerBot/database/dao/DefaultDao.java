package ru.qwonix.tgMoviePlayerBot.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface DefaultDao<T> {
    T convert(ResultSet userResultSet) throws SQLException;

    List<T> findAll() throws SQLException;

    Optional<T> find(long id) throws SQLException;

    void insert(T t) throws SQLException;

    void update(long id, T t) throws SQLException;

    void delete(long id) throws SQLException;
}
