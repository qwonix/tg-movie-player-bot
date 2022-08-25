package ru.qwonix.tgMoviePlayerBot.config;

import java.io.InputStream;
import java.util.Properties;

public abstract class TelegramConfig {
    public static final String BOT_USERNAME = "telegram_bot.username";
    public static final String BOT_TOKEN = "telegram_bot.token";

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
