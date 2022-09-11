package ru.qwonix.tgMoviePlayerBot.bot.command;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SeriesCallback;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.database.DatabaseContext;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    private final DatabaseContext databaseContext;
    private final BotUtils botUtils;

    public BotCommand(BotContext botContext) {
        this.databaseContext = botContext.getDatabaseContext();
        this.botUtils = new BotUtils(botContext);
    }

    public static Method getMethodForCommand(String command) {
        return METHOD_COMMAND.get(command);
    }

    @Command("/start")
    public void start(User user, String[] args) {
        KeyboardRow keyboardButtons = new KeyboardRow();
        keyboardButtons.add(new KeyboardButton("/search"));
        keyboardButtons.add(new KeyboardButton("/all"));

        List<KeyboardRow> keyboardRows = Collections.singletonList(keyboardButtons);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setInputFieldPlaceholder("Выберите режим поиска");

        botUtils.sendMarkdownTextWithKeyBoard(user
                , "Вы можете найти нужную серию по названию или использовать обычный выбор из списка:"
                , replyKeyboardMarkup);

        log.info("start by {}", user);
    }

    @Command("/all")
    public void all(User user, String[] args) {
        List<Series> allSeries = databaseContext.getSeriesService().findAllOrdered();

        StringBuilder sb = new StringBuilder();
        if (allSeries.isEmpty()) {
            botUtils.sendMarkdownText(
                    user
                    , "У нас нету ни одного сериала или фильма, чтобы вам показать :(" +
                            "\n`Загляните попозже`");
        } else {
            Map<String, String> keyboard = new HashMap<>();
            for (Series series : allSeries) {
                String premiereReleaseYearOrTBA = series.getPremiereReleaseYearOrTBA();

                sb.append(String.format("`%s` – *%s* (%s)\n", series.getName(), series.getCountry(), premiereReleaseYearOrTBA));
                sb.append('\n');
                String description = series.getDescription()
                        .substring(0, series.getDescription().indexOf(' ', 90))
                        + "...";
                sb.append(String.format("_%s_\n", description));
                sb.append('\n');

                JSONObject seriesCallback = SeriesCallback.toJson(series.getId(), 0);
                keyboard.put(series.getName() + " (" + premiereReleaseYearOrTBA + ")", seriesCallback.toString());
                List<List<InlineKeyboardButton>> inlineKeyboard = BotUtils.createOneRowCallbackKeyboard(keyboard);
                botUtils.sendMarkdownTextWithKeyBoard(
                        user
                        , sb.toString()
                        , new InlineKeyboardMarkup(inlineKeyboard));
                user.getMessagesIds().reset();
                databaseContext.getUserService().merge(user);
            }
        }
    }

    @Command("/search")
    public void search(User user, String[] args) {
        botUtils.sendMarkdownText(user, "Поиск по названию находится в разработке :(");
        // botUtils.sendMarkdownText(user, "Введите название эпизода\nНапример: `Коллекционер`");
        // user.setStateType(State.StateType.SEARCH);
        // databaseContext.getUserService().merge(user);
    }


    @Command("/admin")
    public void admin(User user, String[] args) {
        if (args.length == 1) {
            String adminPassword = BotConfig.getProperty(BotConfig.ADMIN_PASSWORD);
            if (args[0].equals(adminPassword)) {
                user = databaseContext.getUserService().makeAdmin(user);
                botUtils.sendText(user, "Вы получили права админа! /admin для доступа в меню");
                log.warn("became an admin: {}", user);
            } else {
                log.warn("trying to become an admin: {}", user);
            }
        }
    }

    @Command("/export_video_videofileid")
    public void export_video_videofileid(User user, String[] args) {
        if (!user.isAdmin()) {
            botUtils.sendMarkdownText(user, "Вы не являетесь администратором. Для получения прав используйте /admin <password>");
            return;
        }
        for (Episode e : databaseContext.getEpisodeService().findAll()) {
            botUtils.sendVideoWithMarkdownText(user, String.valueOf(e.getId()), e.getVideoFileId());
        }
        log.info("export by {}", user);
    }

    @Command("/export_video_previewfileid")
    public void export_video_previewid(User user, String[] args) {
        if (!user.isAdmin()) {
            botUtils.sendMarkdownText(user, "Вы не являетесь администратором. Для получения прав используйте /admin <password>");
            return;
        }
        for (Episode e : databaseContext.getEpisodeService().findAll()) {
            botUtils.sendMarkdownTextWithPhoto(user, String.valueOf(e.getId()), e.getPreviewFileId());
        }
        log.info("export by {}", user);
    }

}