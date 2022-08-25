package ru.qwonix.tgMoviePlayerBot.database.dao.series;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.sql.*;
import java.time.LocalDate;
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
                .premiereReleaseDate(this.findPremiereReleaseDate(seriesResultSet.getInt("id")))
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
    public List<Series> findAllOrdered() throws SQLException {
        Connection connection = connectionBuilder.getConnection();
        List<Series> serials = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String SQL = "SELECT * FROM series order by \"order\"";
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
    public List<Series> findAllByNameLikeWithLimitAndPage(String name, int limit, int page) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Series> serials = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM series where lower(name) like ? limit ? offset ?")) {
            preparedStatement.setString(1, "%" + name.toLowerCase() + "%");
            preparedStatement.setInt(2, limit);
            preparedStatement.setInt(3, limit * page);

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
    public List<Series> findAllWithLimitAndPage(int limit, int page) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Series> serials = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM series limit ? offset ?")) {
            preparedStatement.setInt(1, limit);
            preparedStatement.setInt(2, limit * page);

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
    public LocalDate findPremiereReleaseDate(int seriesId) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT min(e.release_date) as premiere_date FROM episode e " +
                             "inner join season s on s.id = e.season_id where s.series_id=?")) {
            preparedStatement.setLong(1, seriesId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            return resultSet.getObject("premiere_date", LocalDate.class);
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
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