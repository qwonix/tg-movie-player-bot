package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.config.DatabaseConfig;
import ru.qwonix.tgMoviePlayerBot.dao.ConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.dao.DaoException;
import ru.qwonix.tgMoviePlayerBot.dao.PoolConnectionBuilder;
import ru.qwonix.tgMoviePlayerBot.dao.SeriesService;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.user.User;
import ru.qwonix.tgMoviePlayerBot.user.UserService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
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
    private final ConnectionBuilder connectionBuilder;

    {
        try {
            connectionBuilder = new PoolConnectionBuilder(
                    DatabaseConfig.getProperty(DatabaseConfig.DB_URL),
                    DatabaseConfig.getProperty(DatabaseConfig.DB_USER),
                    DatabaseConfig.getProperty(DatabaseConfig.DB_PASSWORD),
                    10
            );
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    connectionBuilder.closeConnections();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
    }

    public Bot() {
        userService = new UserService();
        seriesService = new SeriesService(connectionBuilder);
        BotFeatures botFeatures = new BotFeatures(this, userService, seriesService);
        this.botFeatures = botFeatures;

        this.botCommand = new BotCommand(userService, seriesService, botFeatures);
    }

    public void onVideo(Update update) {
        Video video = update.getMessage().getVideo();
        String fileId = video.getFileId();

        // FIXME: 14-Jul-22 проверить, содержит ли update информацию об отправлители. заменить доступ, если нет
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
                .chatId(callbackQuery.getFrom().getId())
                .name(callbackQuery.getFrom().getFirstName())
                .build();

        log.info("user {} callback {}", user, data);
        String fileId = seriesService.findEpisode(Integer.parseInt(data))
                .map(Episode::getFileId)
                .orElse("");

        botFeatures.sendVideo(user, fileId);
    }

    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            onCallbackQuery(update);
            return;
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
            Method commandMethod = commands.get(command);

            if (commandMethod != null) {
                commandMethod.invoke(botCommand, user, commandArgs);
                return;
            }

            this.onNotCommand(user, update);

        } catch (IllegalAccessException e) {
            log.error("reflective access exception" + e.getMessage());
        } catch (InvocationTargetException e) {
            log.error("called method-command threw an exception {}", e.getTargetException().getMessage());
        }
    }

    private void onNotCommand(User user, Update update) {
        botFeatures.sendText(user, "не понял");
    }

    @Override
    public String getBotUsername() {
        return BotConfig.getProperty(BotConfig.BOT_USERNAME);
    }

    @Override
    public String getBotToken() {
        return BotConfig.getProperty(BotConfig.BOT_TOKEN);
    }
}