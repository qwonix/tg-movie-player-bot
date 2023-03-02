package ru.qwonix.tgMoviePlayerBot.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public interface DefaultDao<T> {
    T convert(ResultSet userResultSet) throws SQLException;


    Optional<T> find(long id) throws SQLException;

}
