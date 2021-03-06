package ru.qwonix.tgMoviePlayerBot.bot.command;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.callback.Action;
import ru.qwonix.tgMoviePlayerBot.bot.callback.Callback;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SelectCallback;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SelectCallbackType;
import ru.qwonix.tgMoviePlayerBot.bot.state.UserState;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.dao.DaoContext;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BotCommand {
    private static final Map<String, Method> METHOD_COMMAND = new HashMap<>();

    static {
        for (Method m : BotCommand.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {
                Command command = m.getAnnotation(Command.class);
                METHOD_COMMAND.put(command.value().toLowerCase(), m);
            }
        }
    }

    private final DaoContext daoContext;
    private final BotUtils botUtils;

    public BotCommand(BotUtils botUtils, DaoContext daoContext) {
        this.daoContext = daoContext;
        this.botUtils = botUtils;
    }

    public static Method getMethodForCommand(String command) {
        return METHOD_COMMAND.get(command);
    }

    @Command("/start")
    public void start(User user, String[] args) {
        botUtils.sendText(user, "Доступ получен, для справки используйте /help");
        botUtils.sendText(user, "А пока можете посмотреть Смешариков");
        botUtils.sendVideo(user, "BAACAgIAAxkBAANRYs6x16Dr5lBF8u3hs5Zxxn_ttjMAAnoaAAJq-3BKVr5yOMPU2aQpBA");
        log.info("start by {}", user);
    }

    @Command("/help")
    public void help(User user, String[] args) {
        botUtils.sendMarkdownText(user, "Что надо сделать, если вам навстречу бежит окровавленный негр?\n||*Перезарядить*||");
        log.info("help by {}", user);
    }

    @Command("/all")
    public void all(User user, String[] args) {
        Map<String, String> keyboard = new HashMap<>();

        for (Episode episode : daoContext.getSeriesService().findAllEpisodes()) {
            String data = Callback.convertCallback(Action.SELECT
                    , new SelectCallback(SelectCallbackType.EPISODE, episode.getId()));

            keyboard.put(episode.getName(), data);
        }

        SendMessage.SendMessageBuilder message = SendMessage.builder()
                .text("выбор")
                .replyMarkup(BotUtils.createCallbackKeyboard(keyboard))
                .parseMode("MarkdownV2");

        botUtils.sendMessage(user, message);
    }

    @Command("/search")
    public void search(User user, String[] args) {
        botUtils.sendText(user, "Введите название фильма, сериала или серии!");
        user.setState(UserState.State.SEARCH);
        daoContext.getUserService().merge(user);
    }

    @Command("/admin")
    public void admin(User user, String[] args) {
        if (args.length == 1) {
            String adminPassword = BotConfig.getProperty(BotConfig.ADMIN_PASSWORD);
            if (args[0].equals(adminPassword)) {
                user = daoContext.getUserService().setAdmin(user);
                botUtils.sendText(user, "Вы получили права админа! /admin для доступа в меню");
                log.warn("became an admin: {}", user);
            } else {
                log.warn("trying to become an admin: {}", user);
            }
        }
    }

}