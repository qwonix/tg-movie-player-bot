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

import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class SeasonCallback extends Callback {
    private final BotContext botContext;
    private final ChatContext chatContext;

    public SeasonCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }

    public static JSONObject toJSON(int seasonId) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.SEASON);
        jsonData.put("id", seasonId);

        JSONObject jsonCallback = new JSONObject();
        jsonCallback.put("action", Action.SELECT);
        jsonCallback.put("data", jsonData);

        return jsonCallback;
    }

    @Override
    public void handleCallback(JSONObject callbackData) {
        int seasonId = callbackData.getInt("id");

        SeriesService seriesService = botContext.getDaoContext().getSeriesService();
        Optional<Season> optionalSeason = seriesService.findSeason(seasonId);

        if (optionalSeason.isPresent()) {
            Season season = optionalSeason.get();
            List<Episode> seasonEpisodes = seriesService.findAllBySeasonOrderByNumber(season);

            String text = createText(season, seasonEpisodes);

            Map<String, String> keyboard = new LinkedHashMap<>();
            for (Episode episode : seasonEpisodes) {
                JSONObject episodeCallback = EpisodeCallback.toJSON(episode.getId());
                keyboard.put("Серия " + episode.getNumber() + " «" + episode.getName() + "»", episodeCallback.toString());
            }


            InlineKeyboardMarkup callbackKeyboard = BotUtils.createTwoRowsCallbackKeyboard(keyboard);
            new BotUtils(botContext).sendMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                    , text
                    , callbackKeyboard
                    , season.getPreviewFileId());
        } else {
            String text = "Такого сезона не существует. `Попробуйте найти его заново.`";
            log.error("no season with {} id", seasonId);
            new BotUtils(botContext).sendMarkdownText(chatContext.getUser(), text);
        }
    }

    private String createText(Season season, List<Episode> seasonEpisodes) {
        String seriesPremiereReleaseDate;
        if (season.getPremiereReleaseDate() == null) {
            seriesPremiereReleaseDate = "TBA";
        } else {
            seriesPremiereReleaseDate = season.getPremiereReleaseDate()
                    .format(DateTimeFormatter.ofPattern("d MMMM y", Locale.forLanguageTag("ru")));
        }

        String seriesFinalReleaseDate;
        if (season.getFinalReleaseDate() == null) {
            seriesFinalReleaseDate = "TBA";
        }
        else {
            seriesFinalReleaseDate = season.getFinalReleaseDate()
                    .format(DateTimeFormatter.ofPattern("d MMMM y", Locale.forLanguageTag("ru")));
        }

        return String.format("*%s – %s сезон*\n", season.getSeries().getName(), season.getNumber())
                + '\n'
                + String.format("_%s_\n", season.getDescription())
                + '\n'
                + String.format("*Количество эпизодов*: *%d* / *%s*\n", seasonEpisodes.size(), season.getTotalEpisodesCount())
                + String.format("*Премьера: _%s_*\n", seriesPremiereReleaseDate)
                + String.format("*Финал: _%s_*\n", seriesFinalReleaseDate);
    }
}
