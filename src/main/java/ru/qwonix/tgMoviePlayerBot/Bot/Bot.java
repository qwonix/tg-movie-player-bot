package ru.qwonix.tgMoviePlayerBot.Bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import ru.qwonix.tgMoviePlayerBot.Config.TelegramConfig;
import ru.qwonix.tgMoviePlayerBot.Series.SeriesService;
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

    private final BotCommand botCommand;
    private final BotFeatures botFeatures;
    private final UserService userService;
    private final SeriesService seriesService;

    public Bot() {
        userService = new UserService();
        seriesService = new SeriesService();
        BotFeatures botFeatures = new BotFeatures(this, userService, seriesService);
        this.botFeatures = botFeatures;

        this.botCommand = new BotCommand(userService, botFeatures);
    }

    public void onVideo(Update update) {
        Video video = update.getMessage().getVideo();
        String fileId = video.getFileId();

        User user = User.builder()
                .chatId(update.getMessage().getChatId().intValue())
                .name(update.getMessage().getFrom().getFirstName())
                .build();
        botFeatures.sendVideo(user, fileId);
    }

    public void onCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        User user = User.builder()
                .chatId(update.getMessage().getChatId().intValue())
                .name(update.getMessage().getFrom().getFirstName())
                .build();

        log.info("user {} callback {}", user, data);
        botFeatures.sendVideo(user, data);
    }

    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            onCallbackQuery(update);
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            if (update.getMessage().hasVideo()) {
                this.onVideo(update);
            }
            return;
        }

        User user = User.builder()
                .chatId(update.getMessage().getChatId().intValue())
                .name(update.getMessage().getFrom().getFirstName())
                .build();
        userService.merge(user);
        String userMessageText = update.getMessage().getText();

        log.debug("user: {}, text: {}", user, userMessageText);

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
        return TelegramConfig.getProperty(TelegramConfig.BOT_USERNAME);
    }

    @Override
    public String getBotToken() {
        return TelegramConfig.getProperty(TelegramConfig.BOT_TOKEN);

    }
}