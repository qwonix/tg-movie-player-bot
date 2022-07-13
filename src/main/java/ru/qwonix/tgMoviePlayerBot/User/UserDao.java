package ru.qwonix.tgMoviePlayerBot.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserDao {
    private static final String URL = System.getenv("dbUrl");
    private static final String USERNAME = System.getenv("dbUsername");
    private static final String PASSWORD = System.getenv("dbPassword");

    private static Connection connection;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static User convert(ResultSet userResultSet) throws SQLException {
        return User.builder()
                .chatId(userResultSet.getLong("chat_id"))
                .name(userResultSet.getString("name"))
                .isAdmin(userResultSet.getBoolean("is_admin"))
                .build();
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();

        Statement statement = connection.createStatement();
        String SQL = "SELECT * FROM \"user\"";
        ResultSet resultSet = statement.executeQuery(SQL);

        while (resultSet.next()) {
            User user = convert(resultSet);
            users.add(user);
        }

        return users;
    }

    public Optional<User> find(long id) throws SQLException {
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM \"user\" WHERE chat_id=?");

        preparedStatement.setLong(1, id);

        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            User user = convert(resultSet);
            return Optional.of(user);
        }

        return Optional.empty();
    }

    public void save(User user) throws SQLException {
        PreparedStatement preparedStatement =
                connection.prepareStatement("INSERT INTO \"user\" VALUES(?, ?, ?)");

        preparedStatement.setLong(1, user.getChatId());
        preparedStatement.setString(2, user.getName());
        preparedStatement.setBoolean(3, user.isAdmin());

        preparedStatement.executeUpdate();
    }

    public void update(long id, User updatedUser) throws SQLException {
        PreparedStatement preparedStatement =
                connection.prepareStatement("UPDATE \"user\" SET name=?, is_admin=? WHERE chat_id=?");

        preparedStatement.setString(1, updatedUser.getName());
        preparedStatement.setBoolean(2, updatedUser.isAdmin());
        preparedStatement.setLong(3, id);

        preparedStatement.executeUpdate();
    }

    public void delete(long id) throws SQLException {
        PreparedStatement preparedStatement =
                connection.prepareStatement("DELETE FROM \"user\" WHERE chat_id=?");

        preparedStatement.setLong(1, id);

        preparedStatement.executeUpdate();
    }
}