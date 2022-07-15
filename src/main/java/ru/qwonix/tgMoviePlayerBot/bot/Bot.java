package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.dao.DaoContext;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.User;

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

    private final DaoContext daoContext;


    public Bot() {
        daoContext = new DaoContext();

        BotFeatures botFeatures = new BotFeatures(this, daoContext);
        this.botFeatures = botFeatures;
        this.botCommand = new BotCommand(botFeatures, daoContext);
    }

    public void onVideo(Update update) {
        Video video = update.getMessage().getVideo();
        String fileId = video.getFileId();

        // FIXME: 14-Jul-22 проверить, содержит ли update информацию об отправлители. заменить доступ, если нет
        User user = User.builder()
                .chatId(update.getMessage().getChatId())
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
        String fileId = daoContext.getSeriesService().findEpisode(Integer.parseInt(data))
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

        long chatId = update.getMessage().getChatId();
        User user = User.builder()
                .chatId(chatId)
                .name(update.getMessage().getFrom().getFirstName())
//                .state(new SearchState())
                .build();

        daoContext.getUserService().merge(user);
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