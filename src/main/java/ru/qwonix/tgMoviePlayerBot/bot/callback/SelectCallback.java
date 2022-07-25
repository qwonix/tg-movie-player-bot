package ru.qwonix.tgMoviePlayerBot.bot.callback;

import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.dao.SeriesService;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SelectCallback extends Callback {
    private final BotContext botContext;
    private final ChatContext chatContext;

    public enum DataType {
        SERIES, SEASON, EPISODE
    }

    private static final Map<String, Method> METHOD_CALLBACK = new HashMap<>();

    static {
        for (Method m : SelectCallback.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(CallbackDataType.class)) {
                CallbackDataType callback = m.getAnnotation(CallbackDataType.class);
                METHOD_CALLBACK.put(callback.value().name().toLowerCase(), m);
                System.out.println(callback.value());
            }
        }
    }

    public SelectCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }

    public void action(JSONObject callbackData) {
        String dataType = callbackData.getString("dataType");
        Method callbackMethod = METHOD_CALLBACK.get(dataType.toLowerCase());

        if (callbackMethod != null) {
            try {
                callbackMethod.invoke(this, callbackData);
            } catch (IllegalAccessException | InvocationTargetException ignore) {
            }
        } else {
            new BotUtils(botContext).sendText(chatContext.getUser(), "дефолт блок");
        }
    }

    @CallbackDataType(DataType.EPISODE)
    public void episodeCallback(JSONObject callbackData) {
        int id = callbackData.getInt("id");

        SeriesService seriesService = botContext.getDaoContext().getSeriesService();
        Optional<Episode> optionalEpisode = seriesService.findEpisode(id);
        if (optionalEpisode.isPresent()) {
            Episode episode = optionalEpisode.get();
            new BotUtils(botContext).sendVideo(chatContext.getUser(), episode.getFileId());
        } else {
            String text = "Видео с id " + id + "не найдено. Попробуйте найти его заново.";
            new BotUtils(botContext).sendText(chatContext.getUser(), text);
        }
    }

    @CallbackDataType(DataType.SEASON)
    public void seasonCallback(JSONObject callbackData) {
        int id = callbackData.getInt("id");

        SeriesService seriesService = botContext.getDaoContext().getSeriesService();
        Optional<Season> optionalSeason = seriesService.findSeason(id);
        if (optionalSeason.isPresent()) {
            Season season = optionalSeason.get();

            String sb = String.format("`%s` *%s*", season.getNumber(), season.getPremiereReleaseDate()) +
                    '\n' +
                    '\n' +
                    String.format("_%s_", season.getDescription());

            List<Episode> seasonEpisodes = seriesService.findAllEpisodesBySeason(season);

            Map<String, String> keyboard = new HashMap<>();
            for (Episode episode : seasonEpisodes) {
                String data = Callback.convertCallback(Action.SELECT, DataType.EPISODE, episode.getId());

                keyboard.put("Серия " + episode.getNumber(), data);
            }

            InlineKeyboardMarkup callbackKeyboard = BotUtils.createCallbackKeyboard(keyboard);

            BotUtils botUtils = new BotUtils(botContext);

            String escapedMsg = sb
                    .replace("-", "\\-")
                    .replace("!", "\\!")
                    .replace(".", "\\.");
            botUtils.sendMarkdownTextWithKeyBoard(chatContext.getUser(), escapedMsg, callbackKeyboard);
        } else {
            String text = "Сезона с id " + id + "не найдено. Попробуйте найти его заново.";
            new BotUtils(botContext).sendText(chatContext.getUser(), text);
        }
    }

    @CallbackDataType(DataType.SERIES)
    public void seriesCallback(JSONObject callbackData) {
        int id = callbackData.getInt("id");

        SeriesService seriesService = botContext.getDaoContext().getSeriesService();
        Optional<Series> optionalSeries = seriesService.findSeries(id);
        if (optionalSeries.isPresent()) {
            Series series = optionalSeries.get();

            String sb = String.format("*%s* — `%s`", series.getName(), series.getCountry()) +
                    '\n' +
                    '\n' +
                    String.format("_%s_", series.getDescription());

            List<Season> seriesSeasons = seriesService.findSeasonsBySeries(series);

            Map<String, String> keyboard = new HashMap<>();
            for (Season season : seriesSeasons) {
                String data = Callback.convertCallback(Action.SELECT, DataType.SEASON, season.getId());
                keyboard.put("Сезон " + season.getNumber(), data);
            }

            BotUtils botUtils = new BotUtils(botContext);
            String escapedMsg = sb
                    .replace("-", "\\-")
                    .replace("!", "\\!")
                    .replace(".", "\\.");

            botUtils.sendMarkdownTextWithKeyBoard(chatContext.getUser()
                    , escapedMsg
                    , BotUtils.createCallbackKeyboard(keyboard));
        } else {
            String text = "Сериала с id " + id + "не найдено. Попробуйте найти его заново.";
            new BotUtils(botContext).sendText(chatContext.getUser(), text);
        }
    }
}
