package ru.qwonix.tgMoviePlayerBot.Bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.qwonix.tgMoviePlayerBot.User.User;
import ru.qwonix.tgMoviePlayerBot.User.UserDao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Bot extends TelegramLongPollingBot {

    private static final Map<String, Method> commands = new HashMap<>();

    static {
        for (Method m : BotCommand.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {
                Command cmd = m.getAnnotation(Command.class);
                commands.put(cmd.command(), m);
            }
        }
    }

    private final BotConfig botConfig;
    private final BotCommand botCommand;
    private final BotFeatures botFeatures;

    public Bot(BotConfig botConfig) {
        this.botConfig = botConfig;

        UserDao userDao = new UserDao();
        BotFeatures botFeatures = new BotFeatures(this, userDao);
        this.botFeatures = botFeatures;

        this.botCommand = new BotCommand(userDao, botFeatures);
    }

    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        User user = new User();
        user.setChatId(update.getMessage().getChatId().intValue());

        String userMessageText = update.getMessage().getText();

        String[] allArgs = userMessageText.split(" ");
        String command = allArgs[0].toLowerCase();
        String[] commandArgs = Arrays.copyOfRange(allArgs, 1, allArgs.length);

        try {
            Method method = commands.get(command);

            if (method != null) {
                method.invoke(botCommand, user, commandArgs);
                return;
            }

            botFeatures.sendText(user, "не понял");

        } catch (IllegalAccessException e) {
            log.error("reflective access exception" + e.getMessage());
        } catch (InvocationTargetException e) {
            log.error("called method-command threw an exception {}", e.getTargetException().getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUserName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}