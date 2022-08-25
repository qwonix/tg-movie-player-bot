package ru.qwonix.tgMoviePlayerBot.config;

import java.io.InputStream;
import java.util.Properties;

public abstract class BotConfig {
    public static final String ADMIN_PASSWORD = "admin.password";
    public static final String KEYBOARD_PAGE_EPISODES_MAX = "keyboard.page.episodes.max";
    public static final String KEYBOARD_PAGE_SEASONS_MAX = "keyboard.page.seasons.max";
    public static final String KEYBOARD_COLUMNS_ROW_MAX = "keyboard.columns.row.max";

    private static final Properties properties = new Properties();

    public synchronized static String getProperty(String name) {
        if (properties.isEmpty()) {
            try (InputStream is = DatabaseConfig.class.getClassLoader()
                    .getResourceAsStream("bot.properties")) {
                properties.load(is);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        return properties.getProperty(name);
    }

    public synchronized static int getIntProperty(String name) {
        return Integer.parseInt(BotConfig.getProperty(name));
    }
}
