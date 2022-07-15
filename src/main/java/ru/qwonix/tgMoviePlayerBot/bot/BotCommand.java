package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.dao.DaoContext;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BotCommand {
    private final DaoContext daoContext;
    private final BotFeatures botFeatures;

    public BotCommand(BotFeatures botFeatures, DaoContext daoContext) {
        this.daoContext = daoContext;
        this.botFeatures = botFeatures;
    }

    @Command(command = "/start")
    public void start(User user, String[] args) {
        botFeatures.sendText(user, "Доступ получен, для справки используйте /help");
        botFeatures.sendText(user, "А пока можете посмотреть Смешариков");
        botFeatures.sendVideo(user, "BAACAgIAAxkBAANRYs6x16Dr5lBF8u3hs5Zxxn_ttjMAAnoaAAJq-3BKVr5yOMPU2aQpBA");
        log.info("start by {}", user);
    }

    @Command(command = "/help")
    public void help(User user, String[] args) {
        botFeatures.sendMarkdownText(user, "Что надо сделать, если вам навстречу бежит окровавленный негр?\n||*Перезарядить*||");
        log.info("help by {}", user);
    }

    @Command(command = "/all")
    public void all(User user, String[] args) {
        Map<String, String> ep = new HashMap<>();

        for (Episode episode : daoContext.getSeriesService().findAllEpisodes()) {
            ep.put(episode.getName(), String.valueOf(episode.getId()));
        }

        SendMessage.SendMessageBuilder message = SendMessage.builder()
                .text("выбор")
                .replyMarkup(BotFeatures.createCallbackKeyboard(ep))
                .parseMode("MarkdownV2");

        botFeatures.sendMessage(user, message);
    }

    @Command(command = "/search")
    public void search(User user, String[] args) {
        botFeatures.sendText(user, "Введите название фильма или сериала");

        String desiredContent = String.join(" ", args);
    }

    @Command(command = "/admin")
    public void admin(User user, String[] args) {
        if (args.length == 1) {
            String adminPassword = BotConfig.getProperty(BotConfig.ADMIN_PASSWORD);
            if (args[0].equals(adminPassword)) {
                user = daoContext.getUserService().setAdmin(user);
                botFeatures.sendText(user, "Вы получили права админа! /admin для доступа в меню");
                log.warn("became an admin: {}", user);
            } else {
                log.warn("trying to become an admin: {}", user);
            }
        }
    }

}