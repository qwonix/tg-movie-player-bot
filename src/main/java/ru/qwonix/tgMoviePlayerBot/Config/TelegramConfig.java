package ru.qwonix.tgMoviePlayerBot.Config;

import java.io.InputStream;
import java.util.Properties;

public class TelegramConfig {
    public static final String BOT_USERNAME = "bot.username";
    public static final String BOT_TOKEN = "bot.token";
    public static final String ADMIN_PASSWORD = "admin.password";

    private static final Properties properties = new Properties();

    public synchronized static String getProperty(String name) {
        if (properties.isEmpty()) {
            try (InputStream is = DatabaseConfig.class.getClassLoader()
                    .getResourceAsStream("telegram.properties")) {
                properties.load(is);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        return properties.getProperty(name);
    }
}
