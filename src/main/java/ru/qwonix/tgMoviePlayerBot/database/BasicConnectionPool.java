package ru.qwonix.tgMoviePlayerBot.database;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.config.DatabaseConfig;
import ru.qwonix.tgMoviePlayerBot.exception.DaoException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

@Slf4j
public class BasicConnectionPool implements ConnectionPool {

    private static BasicConnectionPool INSTANCE;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private final int maxPoolSize;

    private final Stack<Connection> availableConnections = new Stack<>();
    private final Set<Connection> usedConnections = new HashSet<>();

    public static ConnectionPool getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new BasicConnectionPool(
                        DatabaseConfig.getProperty(DatabaseConfig.DB_URL),
                        DatabaseConfig.getProperty(DatabaseConfig.DB_USER),
                        DatabaseConfig.getProperty(DatabaseConfig.DB_PASSWORD),
                        10
                );
            } catch (DaoException e) {
                throw new RuntimeException(e);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    INSTANCE.closeConnections();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        return INSTANCE;
    }

    public BasicConnectionPool(String dbUrl, String dbUser, String dbPassword, int maxPoolSize) throws DaoException {
        if (maxPoolSize < 1) {
            throw new DaoException("pool size should be more than 0");
        }
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.maxPoolSize = maxPoolSize;
    }

    @Override
    public Connection getConnection() throws SQLException {
        log.debug("connection request");

        if (usedConnections.size() == maxPoolSize) {
            throw new RuntimeException("no available connections");
        }

        Connection connection;
        if (availableConnections.isEmpty()) {
            log.debug("available connections is empty, creating a new connection");

            try {
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            } catch (SQLException e) {
                log.error("cannot connect to the database", e);
                System.exit(1);
                return null;
            }

            usedConnections.add(connection);
        } else {
            log.debug("there is a available connection");

            connection = availableConnections.pop();
            if (!connection.isValid(2)) {
                log.error("cannot connect to the database");
                System.exit(1);
                return null;
            }

            usedConnections.add(connection);
        }

        log.debug("total connections {}, available {}, used {}"
                , availableConnections.size() + usedConnections.size()
                , availableConnections.size()
                , usedConnections.size());
        return connection;
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        log.debug("release connection {}", connection);

        if (connection == null) {
            return;
        }
        usedConnections.remove(connection);
        if (connection.isClosed() && usedConnections.remove(connection)) {
            log.info("close connection");
            availableConnections.remove(connection);
            return;
        }

        availableConnections.push(connection);
        log.debug("total connections {}, available {}, used {}"
                , availableConnections.size() + usedConnections.size()
                , availableConnections.size()
                , usedConnections.size());

    }

    @Override
    public void closeConnections() throws SQLException {
        for (Connection connection : usedConnections) {
            connection.close();
            usedConnections.remove(connection);
        }
        for (Connection connection : availableConnections) {
            connection.close();
            // FIXME: 12-Aug-22 exception on shutdown
//            availableConnections.remove(connection);
        }
        log.info("all connections have been closed");
    }
}
