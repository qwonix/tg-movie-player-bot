package ru.qwonix.tgMoviePlayerBot.bot.callback;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
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

@AllArgsConstructor
public class SelectCallback extends Callback {
    @SerializedName("dataType")
    private SelectCallbackType selectCallbackType;
    private int id;

    private static final Map<String, Method> METHOD_CALLBACK = new HashMap<>();

    static {
        for (Method m : SelectCallback.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(CallbackAnn.class)) {
                CallbackAnn callback = m.getAnnotation(CallbackAnn.class);
                METHOD_CALLBACK.put(callback.value().toLowerCase(), m);
                System.out.println(callback.value());
            }
        }
    }

    public void action(BotContext botContext, ChatContext chatContext) {
        Method callbackMethod = METHOD_CALLBACK.get(selectCallbackType.toString().toLowerCase());

        boolean b = callbackMethod != null;
        System.out.println(b);
        if (b) {
            try {
                callbackMethod.invoke(this, botContext, chatContext);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            new BotUtils(botContext).sendText(chatContext.getUser(), "дефолт блок");
        }
    }

    @CallbackAnn("episode")
    public void episodeCallback(BotContext botContext, ChatContext chatContext) {
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

    @CallbackAnn("season")
    public void seasonCallback(BotContext botContext, ChatContext chatContext) {
        SeriesService seriesService = botContext.getDaoContext().getSeriesService();
        Optional<Season> optionalSeason = seriesService.findSeason(id);
        if (optionalSeason.isPresent()) {
            Season season = optionalSeason.get();

            String sb = String.format("`%s` *%s*", season.getNumber(), season.getPremiereReleaseDate()) +
                    '\n' +
                    '\n' +
                    String.format("_%s_", season.getDescription()) +
                    '\n' +
                    '\n';

            List<Episode> seasonEpisodes = seriesService.findAllEpisodesBySeason(season);

            Map<String, String> keyboard = new HashMap<>();
            for (Episode episode : seasonEpisodes) {
                String data = Callback.convertCallback(Action.SELECT
                        , new SelectCallback(SelectCallbackType.EPISODE, episode.getId()));
                keyboard.put(String.valueOf(episode.getNumber()), data);
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

    @CallbackAnn("series")
    public void seriesCallback(BotContext botContext, ChatContext chatContext) {
        SeriesService seriesService = botContext.getDaoContext().getSeriesService();
        Optional<Series> optionalSeries = seriesService.findSeries(id);
        if (optionalSeries.isPresent()) {
            Series series = optionalSeries.get();

            String sb = String.format("`%s` *%s*", series.getName(), series.getCountry()) +
                    '\n' +
                    '\n' +
                    String.format("_%s_", series.getDescription()) +
                    '\n' +
                    '\n';

            List<Season> seriesSeasons = seriesService.findSeasonsBySeries(series);

            Map<String, String> keyboard = new HashMap<>();
            for (Season season : seriesSeasons) {
                String data = Callback.convertCallback(Action.SELECT
                        , new SelectCallback(SelectCallbackType.SEASON, season.getId()));
                keyboard.put(String.valueOf(season.getNumber()), data);
            }

            InlineKeyboardMarkup callbackKeyboard = BotUtils.createCallbackKeyboard(keyboard);

            BotUtils botUtils = new BotUtils(botContext);

            String escapedMsg = sb
                    .replace("-", "\\-")
                    .replace("!", "\\!")
                    .replace(".", "\\.");
            botUtils.sendMarkdownTextWithKeyBoard(chatContext.getUser(), escapedMsg, callbackKeyboard);
        } else {
            String text = "Сериала с id " + id + "не найдено. Попробуйте найти его заново.";
            new BotUtils(botContext).sendText(chatContext.getUser(), text);
        }
    }
}
