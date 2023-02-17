package ru.qwonix.tgMoviePlayerBot.database.dao.show;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShowDaoImpl implements ShowDao {
    private final ConnectionBuilder connectionBuilder;

    public ShowDaoImpl(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
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
    public List<Show> findAll() throws SQLException {
        Connection connection = connectionBuilder.getConnection();
        List<Show> serials = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String SQL = "SELECT * FROM show";
            ResultSet resultSet = statement.executeQuery(SQL);

            while (resultSet.next()) {
                Show show = convert(resultSet);
                serials.add(show);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return serials;
    }


    @Override
    public Optional<Show> find(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM show WHERE id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Show show = convert(resultSet);
                return Optional.of(show);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

        return Optional.empty();
    }

    @Override
    public void insert(Show show) throws SQLException {

    }

    @Override
    public void update(long id, Show show) throws SQLException {

    }

    @Override
    public void delete(long id) throws SQLException {

    }

    @Override
    public Optional<Show> findBySeries(long seriesId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM show WHERE id = " +
                             "(select show_id from series where id = ?)")) {
            preparedStatement.setLong(1, seriesId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Show show = convert(resultSet);
                return Optional.of(show);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

        return Optional.empty();
    }
}