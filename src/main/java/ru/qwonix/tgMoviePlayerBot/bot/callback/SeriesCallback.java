package ru.qwonix.tgMoviePlayerBot.bot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;

import java.util.*;

@Slf4j
public class SeriesCallback extends Callback {
    private final BotContext botContext;
    private final ChatContext chatContext;

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

        handleCallback(seriesId, page);
    }

    private void handleCallback(int seriesId, int page) {
        Optional<Series> optionalSeries = botContext.getDatabaseContext().getSeriesService().find(seriesId);

        if (optionalSeries.isPresent()) {
            Series series = optionalSeries.get();
            String text = String.format("*%s*\n", series.getName())
                    + '\n'
                    + String.format("_%s_", series.getDescription());

            int seasonsCount = botContext.getDatabaseContext().getSeasonService().countAllBySeries(series);
            int limit = 1;
            int pagesCount = (int) Math.ceil(seasonsCount / (double) limit);

            List<Season> seriesSeasons = botContext.getDatabaseContext().getSeasonService()
                    .findAllBySeriesOrderByNumberWithLimitAndPage(series, limit, page);

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


            Integer messageIdToDelete = chatContext.getUser().getMessageIdToDelete();
            if (messageIdToDelete != null) {
                new BotUtils(botContext).editMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                        , messageIdToDelete
                        , text
                        , new InlineKeyboardMarkup(inlineKeyboard)
                        , series.getPreviewFileId());

            } else {
                Integer messageId = new BotUtils(botContext).sendMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                        , text
                        , new InlineKeyboardMarkup(inlineKeyboard)
                        , series.getPreviewFileId());
                chatContext.getUser().setMessageIdToDelete(messageId);
                botContext.getDatabaseContext().getUserService().merge(chatContext.getUser());
            }
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
                    .text("×").build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(SeriesCallback.toJson(seriesId, page - 1).toString())
                    .text("‹").build();
        }

        if (pagesCount == page + 1) {
            next = InlineKeyboardButton.builder()
                    .callbackData(SeriesCallback.toJson(seriesId, page).toString())
                    .text("×").build();
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