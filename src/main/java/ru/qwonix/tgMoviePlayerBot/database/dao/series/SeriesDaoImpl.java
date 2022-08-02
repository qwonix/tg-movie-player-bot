package ru.qwonix.tgMoviePlayerBot.database.dao.series;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeriesDaoImpl implements SeriesDao {
    private final ConnectionBuilder connectionBuilder;

    public SeriesDaoImpl(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    @Override
    public Series convert(ResultSet seriesResultSet) throws SQLException {
        return Series.builder()
                .id(seriesResultSet.getInt("id"))
                .name(seriesResultSet.getString("name"))
                .description(seriesResultSet.getString("description"))
                .country(seriesResultSet.getString("country"))
                .previewFileId(seriesResultSet.getString("tg_preview_file_id"))
                .build();
    }

    @Override
    public List<Series> findAll() throws SQLException {
        Connection connection = connectionBuilder.getConnection();
        List<Series> serials = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String SQL = "SELECT * FROM series";
            ResultSet resultSet = statement.executeQuery(SQL);

            while (resultSet.next()) {
                Series series = convert(resultSet);
                serials.add(series);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return serials;
    }

    @Override
    public List<Series> findAllByName(String name) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Series> serials = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM series where name = ?")) {
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Series series = convert(resultSet);
                serials.add(series);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return serials;
    }

    @Override
    public List<Series> findAllByNameLike(String name) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Series> serials = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM series where lower(name) like ?")) {
            preparedStatement.setString(1, "%" + name.toLowerCase() + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Series series = convert(resultSet);
                serials.add(series);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return serials;
    }

    @Override
    public int countAllByNameLike(String name) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT count(*) as match FROM series where lower(name) like ?")) {
            preparedStatement.setString(1, "%" + name.toLowerCase() + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            return resultSet.getInt("match");
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public List<Series> findAllByNameLikeWithLimitAndOffset(String name, int limit, int offset) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Series> serials = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM series where lower(name) like ? limit ? offset ?")) {
            preparedStatement.setString(1, "%" + name.toLowerCase() + "%");
            preparedStatement.setInt(2, limit);
            preparedStatement.setInt(3, offset);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Series series = convert(resultSet);
                serials.add(series);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return serials;
    }

    @Override
    public Optional<Series> find(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM series WHERE id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Series series = convert(resultSet);
                return Optional.of(series);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

        return Optional.empty();
    }

    @Override
    public void insert(Series series) throws SQLException {
        Connection connection = connectionBuilder.getConnection();
        try (PreparedStatement preparedStatement
                     = connection.prepareStatement("INSERT INTO series (name, description, country, tg_preview_file_id) " +
                "VALUES(?, ?, ?, ?)")) {
            preparedStatement.setString(1, series.getName());
            preparedStatement.setString(2, series.getDescription());
            preparedStatement.setString(3, series.getCountry());
            preparedStatement.setString(4, series.getPreviewFileId());

            preparedStatement.executeUpdate();
        }

        connectionBuilder.releaseConnection(connection);
    }

    @Override
    public void update(long id, Series series) throws SQLException {
        Connection connection = connectionBuilder.getConnection();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("UPDATE series SET name=?, description=?, country=?, tg_preview_file_id=? WHERE id=?")) {
            preparedStatement.setString(1, series.getName());
            preparedStatement.setString(2, series.getDescription());
            preparedStatement.setString(3, series.getCountry());
            preparedStatement.setString(4, series.getPreviewFileId());
            preparedStatement.setLong(5, id);
            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    @Override
    public void delete(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("DELETE FROM series WHERE id=?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }
}