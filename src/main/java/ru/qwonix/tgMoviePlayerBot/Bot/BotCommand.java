package ru.qwonix.tgMoviePlayerBot.Bot;

import lombok.extern.slf4j.Slf4j;
import ru.qwonix.tgMoviePlayerBot.User.User;
import ru.qwonix.tgMoviePlayerBot.User.UserService;

@Slf4j
public class BotCommand {
    private final UserService userService;
    private final BotFeatures botFeatures;

    public BotCommand(UserService userService, BotFeatures botFeatures) {
        this.userService = userService;
        this.botFeatures = botFeatures;
    }

    @Command(command = "/start")
    public void start(User user, String[] args) {
        botFeatures.sendText(user, "Доступ получен, до справки используйте /help");
        log.info("start by {}", user);
    }

    @Command(command = "/help")
    public void help(User user, String[] args) {
        botFeatures.sendText((user), "https://youtu.be/zvVtlF0nkR8?t=1");
        log.info("help by {}", user);
    }

    @Command(command = "/admin")
    public void admin(User user, String[] args) {
        if (args.length == 1) {
            String adminPassword = System.getenv("adminPassword");
            if (args[0].equals(adminPassword)) {
                user = userService.setAdmin(user);
                botFeatures.sendText(user, "Вы получили права админа! /admin для доступа в меню");
                log.warn("became an admin: {}", user);
            } else {
                log.warn("trying to become an admin: {}", user);
            }
        }
    }

}