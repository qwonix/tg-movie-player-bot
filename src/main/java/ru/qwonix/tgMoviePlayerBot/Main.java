package ru.qwonix.tgMoviePlayerBot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.qwonix.tgMoviePlayerBot.Bot.Bot;

@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new Bot());
            log.info("telegram init success");

        } catch (TelegramApiRequestException e) {
            log.error("telegram api init error {}", e.getMessage());
        } catch (TelegramApiException e) {
            log.error("telegram init error {}", e.getMessage());
        }
    }
}