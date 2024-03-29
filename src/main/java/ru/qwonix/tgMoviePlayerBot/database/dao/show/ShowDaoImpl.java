package ru.qwonix.tgMoviePlayerBot.database.dao.show;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionPool;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ShowDaoImpl implements ShowDao {
    private final ConnectionPool connectionPool;

    public ShowDaoImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Show convert(ResultSet resultSet) throws SQLException {
        return Show.builder()
                .id(resultSet.getInt("id"))
                .title(resultSet.getString("title"))
                .description(resultSet.getString("description"))
                .previewTgFileId(resultSet.getString("preview_tg_file_id"))
                .build();
    }


    @Override
    public Optional<Show> find(long id) throws SQLException {
        Connection connection = connectionPool.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM show WHERE id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Show show = convert(resultSet);
                return Optional.of(show);
            }
        } finally {
            connectionPool.releaseConnection(connection);
        }

        return Optional.empty();
    }
}