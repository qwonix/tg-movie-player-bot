package ru.qwonix.tgMoviePlayerBot.Bot;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.User.User;
import ru.qwonix.tgMoviePlayerBot.User.UserDao;

@Slf4j
public class BotCommand {
    private final UserDao userDao;
    private final BotFeatures botFeatures;

    public BotCommand(UserDao userDao, BotFeatures botFeatures) {
        this.userDao = userDao;
        this.botFeatures = botFeatures;
    }

    @Command(command = "/start")
    public void start(User user, String[] args) {
        botFeatures.sendText(user, "Напишите /sub, чтобы начать получать уведомления об изменениях. Больше команд по /");
        log.info("start by {}", user.getChatId());
    }

    @Command(command = "/help")
    public void help(User user, String[] args) {
        botFeatures.sendText((user), "https://youtu.be/zvVtlF0nkR8?t=1");
        log.info("help by {}", user.getChatId());
    }
}