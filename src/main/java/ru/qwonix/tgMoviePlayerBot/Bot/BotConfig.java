package ru.qwonix.tgMoviePlayerBot.Bot;

import lombok.Data;

@Data
public class BotConfig {
    private static BotConfig botConfig;
    private final String userName;
    private final String token;

    private BotConfig() {
        userName = System.getenv("botUserName");
        token = System.getenv("botToken");
    }

    public static BotConfig getInstance() {
        if (botConfig == null) {
            botConfig = new BotConfig();
        }
        return botConfig;
    }
}