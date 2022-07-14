package ru.qwonix.tgMoviePlayerBot.user;

import ru.qwonix.tgMoviePlayerBot.dao.ConnectionBuilder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserDao {
    private final ConnectionBuilder connectionBuilder;

    public UserDao(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    public static User convert(ResultSet userResultSet) throws SQLException {
        return User.builder()
                .chatId(userResultSet.getLong("chat_id"))
                .name(userResultSet.getString("name"))
                .isAdmin(userResultSet.getBoolean("is_admin"))
                .build();
    }

    public List<User> findAll() throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<User> users = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String SQL = "SELECT * FROM \"user\"";
            ResultSet resultSet = statement.executeQuery(SQL);

            while (resultSet.next()) {
                User user = convert(resultSet);
                users.add(user);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return users;
    }

    public Optional<User> find(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM \"user\" WHERE chat_id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                User user = convert(resultSet);
                return Optional.of(user);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }


        return Optional.empty();
    }

    public void insert(User user) throws SQLException {
        Connection connection = connectionBuilder.getConnection();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("INSERT INTO \"user\" VALUES(?, ?, ?)")) {

            preparedStatement.setLong(1, user.getChatId());
            preparedStatement.setString(2, user.getName());
            preparedStatement.setBoolean(3, user.isAdmin());

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

    }

    public void update(long id, User updatedUser) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("UPDATE \"user\" SET name=?, is_admin=? WHERE chat_id=?")) {

            preparedStatement.setString(1, updatedUser.getName());
            preparedStatement.setBoolean(2, updatedUser.isAdmin());
            preparedStatement.setLong(3, id);

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

    }

    public void delete(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("DELETE FROM \"user\" WHERE chat_id=?")) {
            preparedStatement.setLong(1, id);

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }
}