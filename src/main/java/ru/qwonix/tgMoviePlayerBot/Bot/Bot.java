package ru.qwonix.tgMoviePlayerBot.Bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.qwonix.tgMoviePlayerBot.User.User;
import ru.qwonix.tgMoviePlayerBot.User.UserService;

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
    private final UserService userService;

    public Bot(BotConfig botConfig) {
        this.botConfig = botConfig;

        userService = new UserService();
        BotFeatures botFeatures = new BotFeatures(this, userService);
        this.botFeatures = botFeatures;

        this.botCommand = new BotCommand(userService, botFeatures);
    }

    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        User user = User.builder()
                .chatId(update.getMessage().getChatId().intValue())
                .name(update.getMessage().getFrom().getFirstName())
                .build();
        userService.merge(user);

        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();

            System.out.println(data); //////
        }

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