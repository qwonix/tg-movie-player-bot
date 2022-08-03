package ru.qwonix.tgMoviePlayerBot.bot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonService;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesService;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.util.*;

@Slf4j
public class SeriesCallback extends Callback {
    private final BotContext botContext;
    private final ChatContext chatContext;
    private static final String lockCharacter = "×";

    public SeriesCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }

    public static JSONObject toJson(int seriesId, int page) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.SERIES);
        jsonData.put("id", seriesId);
        jsonData.put("page", page);

        return Callback.toCallbackJson(jsonData);
    }

    @Override
    public void handleCallback(JSONObject callbackData) {
        int seriesId = callbackData.getInt("id");
        int page = callbackData.getInt("page");

        SeasonService seasonService = botContext.getDatabaseContext().getSeasonService();
        SeriesService seriesService = botContext.getDatabaseContext().getSeriesService();

        Optional<Series> optionalSeries = seriesService.find(seriesId);

        if (optionalSeries.isPresent()) {
            Series series = optionalSeries.get();

            int seasonsCount = seasonService.countAllBySeries(series);
            int limit = 1;
            int pagesCount = (int) Math.ceil(seasonsCount / (double) limit);

            String text = String.format("*%s*\n", series.getName())
                    + '\n'
                    + String.format("_%s_", series.getDescription());

            List<Season> seriesSeasons = seasonService.findAllBySeriesOrderByNumberWithLimitAndPage(series, limit, page);
            Map<String, String> keyboard = new LinkedHashMap<>();
            for (Season season : seriesSeasons) {
                JSONObject callbackSeason = SeasonCallback.toJson(season.getId(), 0);
                keyboard.put("Сезон " + season.getNumber(), callbackSeason.toString());
            }
            List<List<InlineKeyboardButton>> inlineKeyboard = BotUtils.createTwoRowsCallbackKeyboard(keyboard);

            if (pagesCount > 1) {
                List<InlineKeyboardButton> controlButtons = createControlButtons(seriesId, pagesCount, page);
                inlineKeyboard.add(controlButtons);
            }

            new BotUtils(botContext).sendMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                    , text
                    , new InlineKeyboardMarkup(inlineKeyboard)
                    , series.getPreviewFileId());
        } else {
            String text = "Такого сериала не существует. `Попробуйте изменить запрос.`";
            log.error("no series with {} id", seriesId);
            new BotUtils(botContext).sendMarkdownText(chatContext.getUser(), text);
        }
    }

    private List<InlineKeyboardButton> createControlButtons(int seriesId, int pagesCount, int page) {
        InlineKeyboardButton previous;
        InlineKeyboardButton next;

        if (page == 0) {
            previous = InlineKeyboardButton.builder()
                    .callbackData(SeriesCallback.toJson(seriesId, page).toString())
                    .text(lockCharacter).build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(SeriesCallback.toJson(seriesId, page - 1).toString())
                    .text("‹").build();
        }

        if (pagesCount == page + 1) {
            next = InlineKeyboardButton.builder()
                    .callbackData(SeriesCallback.toJson(seriesId, page).toString())
                    .text(lockCharacter).build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData(SeriesCallback.toJson(seriesId, page + 1).toString())
                    .text("›").build();
        }

        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData(SeriesCallback.toJson(seriesId, page).toString())
                .text(page + 1 + "/" + pagesCount).build();

        return Arrays.asList(previous, current, next);
    }
}