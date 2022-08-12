package ru.qwonix.tgMoviePlayerBot.config;

import java.io.InputStream;
import java.util.Properties;

public abstract class DatabaseConfig {
    public static final String DB_URL = "db.url";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";

    private static final Properties properties = new Properties();

    public synchronized static String getProperty(String name) {
        if (properties.isEmpty()) {
            try (InputStream is = DatabaseConfig.class.getClassLoader()
                    .getResourceAsStream("database.properties")) {
                properties.load(is);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        return properties.getProperty(name);
    }
}
