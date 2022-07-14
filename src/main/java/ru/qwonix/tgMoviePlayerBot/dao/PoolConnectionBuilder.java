
package ru.qwonix.tgMoviePlayerBot.dao;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

@Slf4j
public class PoolConnectionBuilder implements ConnectionBuilder {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private final int maxPoolSize;

    private final Stack<Connection> availableConnections = new Stack<>();
    private final Set<Connection> usedConnections = new HashSet<>();

    public PoolConnectionBuilder(String dbUrl, String dbUser, String dbPassword, int maxPoolSize) throws DaoException {
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
        log.debug("всего коннектов {}, доступно {}, используется {}"
                , availableConnections.size() + usedConnections.size()
                , availableConnections.size()
                , usedConnections.size());

        log.debug("запрос коннекта");
        if (usedConnections.size() == maxPoolSize)
            throw new RuntimeException("no available connections");

        Connection connection;
        if (availableConnections.isEmpty()) {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            usedConnections.add(connection);
        } else {
            connection = availableConnections.pop();
            usedConnections.add(connection);
        }
        log.debug("коннекта выдан {}", connection);
        return connection;
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        log.debug("коннект принят {}", connection);
        log.debug("всего коннектов {}, доступно {}, используется {}"
                , availableConnections.size() + usedConnections.size()
                , availableConnections.size()
                , usedConnections.size());

        if (connection == null) {
            return;
        }
        usedConnections.remove(connection);
        if (connection.isClosed() && usedConnections.remove(connection)) {
            log.info("коннект убран");
            availableConnections.remove(connection);
            return;
        }

        availableConnections.push(connection);
        log.debug("коннект запушен {}", connection);
    }
}
