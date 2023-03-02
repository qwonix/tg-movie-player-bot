package ru.qwonix.tgMoviePlayerBot.database.dao.series;

import ru.qwonix.tgMoviePlayerBot.database.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.database.dao.show.ShowDao;
import ru.qwonix.tgMoviePlayerBot.database.dao.show.ShowDaoImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.Show;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class SeriesDaoImpl implements SeriesDao {
    private final ConnectionBuilder connectionBuilder;

    public SeriesDaoImpl(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    @Override
    public Series convert(ResultSet resultSet) throws SQLException {
        ShowDao showDao = new ShowDaoImpl(connectionBuilder);
        Optional<Show> show = showDao.find(resultSet.getInt("show_id"));

        return Series.builder()
                .id(resultSet.getInt("id"))
                .title(resultSet.getString("title"))
                .description(resultSet.getString("description"))
                .country(resultSet.getString("country"))
                .previewTgFileId(resultSet.getString("preview_tg_file_id"))
                .premiereReleaseDate(this.findPremiereReleaseDate(resultSet.getInt("id")))
                .show(show.orElse(null))
                .build();
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
}