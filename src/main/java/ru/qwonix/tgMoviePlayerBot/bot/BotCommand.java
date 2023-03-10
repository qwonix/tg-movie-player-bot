package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.qwonix.tgMoviePlayerBot.callback.MovieCallback;
import ru.qwonix.tgMoviePlayerBot.callback.SeasonCallback;
import ru.qwonix.tgMoviePlayerBot.callback.SeriesCallback;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.database.BasicConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.service.movie.MovieService;
import ru.qwonix.tgMoviePlayerBot.database.service.movie.MovieServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonService;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesService;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserService;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserServiceImpl;
import ru.qwonix.tgMoviePlayerBot.entity.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Command {
    String value();
}

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

    private final BotUtils botUtils = new BotUtils(Bot.getInstance());
    private final UserService userService = new UserServiceImpl(BasicConnectionPool.getInstance());
    private final SeasonService seasonService = new SeasonServiceImpl(BasicConnectionPool.getInstance());
    private final SeriesService seriesService = new SeriesServiceImpl(BasicConnectionPool.getInstance());
    private final MovieService movieService = new MovieServiceImpl(BasicConnectionPool.getInstance());


    public static Method getMethodForCommand(String command) {
        return METHOD_COMMAND.get(command);
    }

    @Command("/start")
    public void start(User user, String[] args) {
        KeyboardRow keyboardButtons = new KeyboardRow();
        keyboardButtons.add(new KeyboardButton("/search (в разработке)"));
        keyboardButtons.add(new KeyboardButton("/all"));

        List<KeyboardRow> keyboardRows = Collections.singletonList(keyboardButtons);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setInputFieldPlaceholder("Выберите режим поиска");

        botUtils.sendMarkdownTextWithKeyBoard(user
                , "Вы можете найти нужную серию, используя команду /all"
                , replyKeyboardMarkup);

        log.info("start by {}", user);
    }

    @Command("/all")
    public void all(User user, String[] args) {
        botUtils.deleteMessageIds(user, user.getMessagesIds());
        user.getMessagesIds().reset();
        userService.merge(user);
        List<List<InlineKeyboardButton>> moviesKeyboard;
        {
            Map<String, String> keyboardMap = new LinkedHashMap<>();

            for (Movie movie : movieService.findByShow(Show.builder().id(1).build())) {
                JSONObject callbackSeason = MovieCallback.toJson(movie.getId());
                keyboardMap.put(movie.getTitle(), callbackSeason.toString());
            }
            moviesKeyboard = BotUtils.createOneRowCallbackKeyboard(keyboardMap);
        }

        Optional<Series> optionalSeries = seriesService.find(1);
        Series series = optionalSeries.get();
        int page = 0;

        String text = String.format("*%s*\n", series.getTitle())
                + '\n'
                + String.format("_%s_", series.getDescription());

        List<List<InlineKeyboardButton>> seasonsKeyboard;

        {
            Map<String, String> keyboardMap = new LinkedHashMap<>();

            int seasonsCount = seasonService.countAllBySeries(series);
            int limit = Integer.parseInt(BotConfig.getProperty(BotConfig.KEYBOARD_PAGE_SEASONS_MAX));
            int pagesCount = (int) Math.ceil(seasonsCount / (double) limit);
            List<Season> seriesSeasons = seasonService.findAllBySeriesOrderByNumberWithLimitAndPage(series, limit, page);

            for (Season season : seriesSeasons) {
                JSONObject callbackSeason = SeasonCallback.toJson(season.getId(), 0);
                keyboardMap.put("Сезон " + season.getNumber(), callbackSeason.toString());
            }
            seasonsKeyboard = BotUtils.createTwoRowsCallbackKeyboard(keyboardMap);

            if (pagesCount > 1) {
                List<InlineKeyboardButton> controlButtons = SeriesCallback.createControlButtons(series.getId(), pagesCount, page);
                seasonsKeyboard.add(controlButtons);
            }
        }

        moviesKeyboard.addAll(seasonsKeyboard);
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(moviesKeyboard);

        Integer seriesMessageId = botUtils.sendPhotoWithMarkdownTextAndKeyboard(user
                , text
                , series.getPreviewTgFileId()
                , keyboard);


        /*List<Series> allSeries = databaseContext.getSeriesService().findAllOrdered();

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
        }*/
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
                user = userService.makeAdmin(user);
                botUtils.sendMarkdownText(user, "Вы получили права админа! /admin для доступа в меню");
                log.warn("became an admin: {}", user);
            } else {
                log.warn("trying to become an admin: {}", user);
            }
        }
    }

//    @Command("/export_video_videofileid")
//    public void export_video_videofileid(User user, String[] args) {
//        if (!user.isAdmin()) {
//            botUtils.sendMarkdownText(user, "Вы не являетесь администратором. Для получения прав используйте /admin <password>");
//            return;
//        }
//        for (Episode e : databaseContext.getEpisodeService().findAll()) {
//            botUtils.sendVideoWithMarkdownText(user, String.valueOf(e.getId()), e.getVideoFileId());
//        }
//        log.info("export by {}", user);
//    }
//
//    @Command("/export_video_previewfileid")
//    public void export_video_previewfileid(User user, String[] args) {
//        if (!user.isAdmin()) {
//            botUtils.sendMarkdownText(user, "Вы не являетесь администратором. Для получения прав используйте /admin <password>");
//            return;
//        }
//        for (Episode e : databaseContext.getEpisodeService().findAll()) {
//            botUtils.sendMarkdownTextWithPhoto(user, String.valueOf(e.getId()), e.getPreviewFileId());
//        }
//        log.info("export by {}", user);
//    }

}