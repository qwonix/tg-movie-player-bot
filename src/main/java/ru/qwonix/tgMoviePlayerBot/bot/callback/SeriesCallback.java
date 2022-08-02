package ru.qwonix.tgMoviePlayerBot.bot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonService;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesService;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SeriesCallback extends Callback {
    private final BotContext botContext;
    private final ChatContext chatContext;

    public SeriesCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }

    public static JSONObject toJson(int seriesId) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.SERIES);
        jsonData.put("id", seriesId);

        return Callback.toCallbackJson(jsonData);
    }

    @Override
    public void handleCallback(JSONObject callbackData) {
        int seriesId = callbackData.getInt("id");

        SeasonService seasonService = botContext.getDatabaseContext().getSeasonService();
        SeriesService seriesService = botContext.getDatabaseContext().getSeriesService();

        Optional<Series> optionalSeries = seriesService.findSeries(seriesId);

        if (optionalSeries.isPresent()) {
            Series series = optionalSeries.get();

            String text = String.format("*%s*\n", series.getName())
                    + '\n'
                    + String.format("_%s_", series.getDescription());

            Map<String, String> keyboard = new LinkedHashMap<>();
            List<Season> seriesSeasons = seasonService.findSeasonsBySeriesOrderByNumber(series);
            for (Season season : seriesSeasons) {
                JSONObject callbackSeason = SeasonCallback.toJSON(season.getId(), 0);
                keyboard.put("Сезон " + season.getNumber(), callbackSeason.toString());
            }

            new BotUtils(botContext).sendMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                    , text
                    , BotUtils.createTwoRowsCallbackKeyboard(keyboard)
                    , series.getPreviewFileId());
        } else {
            String text = "Такого сериала не существует. `Попробуйте изменить запрос.`";
            log.error("no series with {} id", seriesId);
            new BotUtils(botContext).sendMarkdownText(chatContext.getUser(), text);
        }
    }

}
