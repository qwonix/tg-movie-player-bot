package ru.qwonix.tgMoviePlayerBot.bot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.database.servie.SeriesService;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class SelectCallback extends Callback {
    private static final Map<DataType, Method> DATATYPE_METHOD = new HashMap<>();

    static {
        for (Method m : SelectCallback.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(CallbackDataType.class)) {
                CallbackDataType callback = m.getAnnotation(CallbackDataType.class);
                DATATYPE_METHOD.put(callback.value(), m);
            }
        }
    }

    private final SelectCallback.DataType dataType;
    private final int id;
    private BotContext botContext;
    private ChatContext chatContext;

    public SelectCallback(DataType dataType, int id) {
        this.dataType = dataType;
        this.id = id;
    }

    public SelectCallback(JSONObject callbackData) {
        this.dataType = DataType.valueOf(callbackData.getString("dataType"));
        this.id = callbackData.getInt("id");
    }

    public void handle(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;

        Method callbackMethod = DATATYPE_METHOD.get(dataType);

        if (callbackMethod != null) {
            try {
                callbackMethod.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException ignore) {
            }
        } else {
            log.error("no annotated callback method for {}", dataType);
        }
    }

    @CallbackDataType(DataType.EPISODE)
    private void episodeCallback() {
        SeriesService seriesService = botContext.getDaoContext().getSeriesService();
        Optional<Episode> optionalEpisode = seriesService.findEpisode(id);

        if (optionalEpisode.isPresent()) {
            Episode episode = optionalEpisode.get();

            String text = String.format("`%s сезон %s серия` – *%s*", episode.getSeason().getNumber(), episode.getNumber(), episode.getName()) +
                    '\n' +
                    '\n' +
                    String.format("_%s_", episode.getDescription()) +
                    '\n' +
                    '\n' +
                    String.format("Дата выхода: %s года  %s (%s)", episode.getReleaseDate().format(DateTimeFormatter.ofPattern("d MM y")), episode.getCountry(), episode.getLanguage());

            BotUtils botUtils = new BotUtils(botContext);
            botUtils.sendMarkdownTextWithPhoto(chatContext.getUser()
                    , text
                    , episode.getPreviewFileId());
            botUtils.sendVideo(chatContext.getUser(), episode.getVideoFileId());
        } else {
            String text = "Видео с id " + id + "не найдено. Попробуйте найти его заново.";
            new BotUtils(botContext).sendText(chatContext.getUser(), text);
        }
    }

    @CallbackDataType(DataType.SEASON)
    private void seasonCallback() {
        SeriesService seriesService = botContext.getDaoContext().getSeriesService();
        Optional<Season> optionalSeason = seriesService.findSeason(id);

        if (optionalSeason.isPresent()) {
            Season season = optionalSeason.get();

            String text = String.format("`%s сезон` *%s*", season.getNumber(), season.getPremiereReleaseDate().format(DateTimeFormatter.ofPattern("d MM y"))) +
                    '\n' +
                    '\n' +
                    String.format("_%s_", season.getDescription());

            List<Episode> seasonEpisodes = seriesService.findAllEpisodesBySeason(season);

            Map<String, String> keyboard = new LinkedHashMap<>();
            for (Episode episode : seasonEpisodes) {
                SelectCallback data = new SelectCallback(DataType.EPISODE, episode.getId());
                keyboard.put("Серия " + episode.getNumber(), data.toJSON().toString());
            }

            InlineKeyboardMarkup callbackKeyboard = BotUtils.createCallbackKeyboard(keyboard);
            new BotUtils(botContext).sendMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                    , text
                    , callbackKeyboard
                    , season.getPreviewFileId());
        } else {
            String text = "Сезона с id " + id + "не найдено. Попробуйте найти его заново.";
            new BotUtils(botContext).sendText(chatContext.getUser(), text);
        }
    }

    @CallbackDataType(DataType.SERIES)
    private void seriesCallback() {
        SeriesService seriesService = botContext.getDaoContext().getSeriesService();
        Optional<Series> optionalSeries = seriesService.findSeries(id);

        if (optionalSeries.isPresent()) {
            Series series = optionalSeries.get();

            String text = String.format("*%s* — `%s`", series.getName(), series.getCountry()) +
                    '\n' +
                    '\n' +
                    String.format("_%s_", series.getDescription());

            Map<String, String> keyboard = new LinkedHashMap<>();
            List<Season> seriesSeasons = seriesService.findSeasonsBySeries(series);
            for (Season season : seriesSeasons) {
                SelectCallback data = new SelectCallback(DataType.SEASON, season.getId());
                keyboard.put("Сезон " + season.getNumber(), data.toJSON().toString());
            }

            new BotUtils(botContext).sendMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                    , text
                    , BotUtils.createCallbackKeyboard(keyboard)
                    , series.getPreviewFileId());
        } else {
            String text = "Сериала с id " + id + "не найдено. Попробуйте найти его заново.";
            new BotUtils(botContext).sendText(chatContext.getUser(), text);
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", dataType.name());
        jsonData.put("id", id);

        JSONObject jsonCallback = new JSONObject();
        jsonCallback.put("action", Action.SELECT.toString());
        jsonCallback.put("data", jsonData);

        return jsonCallback;
    }

    public enum DataType {
        SERIES, SEASON, EPISODE
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface CallbackDataType {
        SelectCallback.DataType value();
    }
}
