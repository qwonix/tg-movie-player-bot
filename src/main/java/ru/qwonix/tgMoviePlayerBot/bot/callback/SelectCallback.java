package ru.qwonix.tgMoviePlayerBot.bot.callback;

import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.database.servie.SeriesServiceImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
            new BotUtils(botContext).sendText(chatContext.getUser(), "дефолт блок");
        }
    }

    @CallbackDataType(DataType.EPISODE)
    private void episodeCallback() {
        SeriesServiceImpl seriesServiceImpl = botContext.getDaoContext().getSeriesServiceImpl();
        Optional<Episode> optionalEpisode = seriesServiceImpl.findEpisode(id);
        if (optionalEpisode.isPresent()) {
            Episode episode = optionalEpisode.get();
            String sb = String.format("`%s сезон %s серия` – *%s*", episode.getSeason().getNumber(), episode.getNumber(), episode.getName()) +
                    '\n' +
                    '\n' +
                    String.format("_%s_", episode.getDescription()) +
                    '\n' +
                    '\n' +
                    String.format("Дата выхода: %s года  %s (%s)", episode.getReleaseDate().format(DateTimeFormatter.ofPattern("d MM y")), episode.getCountry(), episode.getLanguage());


            BotUtils botUtils = new BotUtils(botContext);
            botUtils.sendMarkdownText(chatContext.getUser(), sb);
            botUtils.sendVideo(chatContext.getUser(), episode.getFileId());
        } else {
            String text = "Видео с id " + id + "не найдено. Попробуйте найти его заново.";
            new BotUtils(botContext).sendText(chatContext.getUser(), text);
        }
    }

    @CallbackDataType(DataType.SEASON)
    private void seasonCallback() {
        SeriesServiceImpl seriesServiceImpl = botContext.getDaoContext().getSeriesServiceImpl();
        Optional<Season> optionalSeason = seriesServiceImpl.findSeason(id);
        if (optionalSeason.isPresent()) {
            Season season = optionalSeason.get();

            String sb = String.format("`%s сезон` *%s*", season.getNumber(), season.getPremiereReleaseDate().format(DateTimeFormatter.ofPattern("d MM y"))) +
                    '\n' +
                    '\n' +
                    String.format("_%s_", season.getDescription());

            List<Episode> seasonEpisodes = seriesServiceImpl.findAllEpisodesBySeason(season);

            Map<String, String> keyboard = new LinkedHashMap<>();
            for (Episode episode : seasonEpisodes) {
                SelectCallback data = new SelectCallback(DataType.EPISODE, episode.getId());
                keyboard.put("Серия " + episode.getNumber(), data.toJSON().toString());
            }

            InlineKeyboardMarkup callbackKeyboard = BotUtils.createCallbackKeyboard(keyboard);

            BotUtils botUtils = new BotUtils(botContext);

            botUtils.sendMarkdownTextWithKeyBoard(chatContext.getUser(), sb, callbackKeyboard);
        } else {
            String text = "Сезона с id " + id + "не найдено. Попробуйте найти его заново.";
            new BotUtils(botContext).sendText(chatContext.getUser(), text);
        }
    }

    @CallbackDataType(DataType.SERIES)
    private void seriesCallback() {
        SeriesServiceImpl seriesServiceImpl = botContext.getDaoContext().getSeriesServiceImpl();
        Optional<Series> optionalSeries = seriesServiceImpl.findSeries(id);
        if (optionalSeries.isPresent()) {
            Series series = optionalSeries.get();

            String sb = String.format("*%s* — `%s`", series.getName(), series.getCountry()) +
                    '\n' +
                    '\n' +
                    String.format("_%s_", series.getDescription());

            List<Season> seriesSeasons = seriesServiceImpl.findSeasonsBySeries(series);

            Map<String, String> keyboard = new LinkedHashMap<>();
            for (Season season : seriesSeasons) {
                SelectCallback data = new SelectCallback(DataType.SEASON, season.getId());
                keyboard.put("Сезон " + season.getNumber(), data.toJSON().toString());
            }

            BotUtils botUtils = new BotUtils(botContext);

            botUtils.sendMarkdownTextWithKeyBoard(chatContext.getUser()
                    , sb
                    , BotUtils.createCallbackKeyboard(keyboard));
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
}
