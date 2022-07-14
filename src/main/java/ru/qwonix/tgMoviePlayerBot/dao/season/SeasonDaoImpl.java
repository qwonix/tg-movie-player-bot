package ru.qwonix.tgMoviePlayerBot.dao.season;

import ru.qwonix.tgMoviePlayerBot.dao.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.dao.series.SeriesDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.dao.series.SeriesDao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeasonDaoImpl implements SeasonDao {

    private final ConnectionBuilder connectionBuilder;

    public SeasonDaoImpl(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    public Season convert(ResultSet seasonResultSet) throws SQLException {
        SeriesDao seriesDao = new SeriesDaoImpl(connectionBuilder);
        Optional<Series> series = seriesDao.find(seasonResultSet.getInt("series_id"));

        return Season.builder()
                .id(seasonResultSet.getInt("id"))
                .number(seasonResultSet.getInt("number"))
                .description(seasonResultSet.getString("description"))
                .premiereReleaseDate(seasonResultSet.getObject("premiere_release_date", LocalDate.class))
                .finalReleaseDate(seasonResultSet.getObject("final_release_date", LocalDate.class))
                .series(series.orElse(null))
                .build();
    }

    public List<Season> findAll() throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Season> seasons = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String SQL = "SELECT * FROM season";
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                Season season = convert(resultSet);
                seasons.add(season);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return seasons;
    }

    public List<Season> findAllBySeries(Series series) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        List<Season> seasons = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM season where series_id = ?")) {
            preparedStatement.setInt(1, series.getId());

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Season season = convert(resultSet);
                seasons.add(season);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
        return seasons;
    }

    public Optional<Season> find(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM season WHERE id=?")) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Season season = convert(resultSet);
                return Optional.of(season);
            }
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

        return Optional.empty();
    }

    public void insert(Season season) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("INSERT INTO season (number, description, premiere_release_date, final_release_date, series_id) " +
                             "VALUES(?, ?, ?, ?, ?)")) {

            preparedStatement.setInt(1, season.getNumber());
            preparedStatement.setString(2, season.getDescription());
            preparedStatement.setObject(3, season.getPremiereReleaseDate());
            preparedStatement.setObject(4, season.getFinalReleaseDate());
            preparedStatement.setInt(5, season.getSeries().getId());

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    public void update(long id, Season season) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("UPDATE season SET number=?, description=?, premiere_release_date=?, final_release_date=?,series_id=? WHERE id=?")) {

            preparedStatement.setInt(1, season.getNumber());
            preparedStatement.setString(2, season.getDescription());
            preparedStatement.setObject(3, season.getPremiereReleaseDate());
            preparedStatement.setObject(4, season.getFinalReleaseDate());
            preparedStatement.setLong(5, season.getSeries().getId());
            preparedStatement.setLong(6, id);

            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }
    }

    public void delete(long id) throws SQLException {
        Connection connection = connectionBuilder.getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("DELETE FROM season WHERE id=?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } finally {
            connectionBuilder.releaseConnection(connection);
        }

    }
}
