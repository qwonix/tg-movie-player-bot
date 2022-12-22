package ru.qwonix.tgMoviePlayerBot.bot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
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

        return Callback.toCallback(jsonData);
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
            this.onSeriesExists(optionalSeries.get(), page);

        } else {
            new BotUtils(botContext).executeAlertWithText(chatContext.getUpdate().getCallbackQuery().getId()
                    , "Такого сериала не существует. Попробуйте найти его заново."
                    , false);
            log.error("no series with {} id", seriesId);
        }
    }

    private void onSeriesExists(Series series, int page) {
        BotUtils botUtils = new BotUtils(botContext);
        String text = String.format("*%s*\n", series.getName())
                + '\n'
                + String.format("_%s_", series.getDescription());

        int seasonsCount = botContext.getDatabaseContext().getSeasonService().countAllBySeries(series);
        int limit = Integer.parseInt(BotConfig.getProperty(BotConfig.KEYBOARD_PAGE_SEASONS_MAX));
        int pagesCount = (int) Math.ceil(seasonsCount / (double) limit);

        List<Season> seriesSeasons = botContext.getDatabaseContext().getSeasonService()
                .findAllBySeriesOrderByNumberWithLimitAndPage(series, limit, page);

        InlineKeyboardMarkup keyboard;
        if (seriesSeasons.isEmpty()) {
            botUtils.executeAlertWithText(chatContext.getUpdate().getCallbackQuery().getId()
                    , "Информации о сезонах нет"
                    , true);
            keyboard = new InlineKeyboardMarkup(Collections.emptyList());

        } else {
            Map<String, String> keyboardMap = new LinkedHashMap<>();
            for (Season season : seriesSeasons) {
                JSONObject callbackSeason = SeasonCallback.toJson(season.getId(), 0);
                keyboardMap.put("Сезон " + season.getNumber(), callbackSeason.toString());
            }
            List<List<InlineKeyboardButton>> inlineKeyboard = BotUtils.createTwoRowsCallbackKeyboard(keyboardMap);

            if (pagesCount > 1) {
                List<InlineKeyboardButton> controlButtons = createControlButtons(series.getId(), pagesCount, page);
                inlineKeyboard.add(controlButtons);
            }
            keyboard = new InlineKeyboardMarkup(inlineKeyboard);
        }

        MessagesIds messagesIds = chatContext.getUser().getMessagesIds();

        if (messagesIds.hasSeasonMessageId()) {
            botUtils.deleteMessage(chatContext.getUser(), messagesIds.getSeasonMessageId());
            messagesIds.setSeasonMessageId(null);
        }
        if (messagesIds.hasEpisodeMessageId()) {
            botUtils.deleteMessage(chatContext.getUser(), messagesIds.getEpisodeMessageId());
            messagesIds.setEpisodeMessageId(null);
        }
        if (messagesIds.hasVideoMessageId()) {
            botUtils.deleteMessage(chatContext.getUser(), messagesIds.getVideoMessageId());
            messagesIds.setVideoMessageId(null);
        }
        if (messagesIds.hasSeriesMessageId()) {
            botUtils.deleteMessage(chatContext.getUser(), messagesIds.getSeriesMessageId());
            messagesIds.setSeriesMessageId(null);
        }

        if (messagesIds.hasSeriesMessageId()) {
            botUtils.editKeyBoardAndPhoto(chatContext.getUser()
                    , messagesIds.getSeriesMessageId()
                    , keyboard
                    , series.getPreviewFileId());
        } else {
            Integer seriesMessageId = botUtils.sendMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                    , text
                    , series.getPreviewFileId()
                    , keyboard);

            messagesIds.setSeriesMessageId(seriesMessageId);
        }

        botContext.getDatabaseContext().getUserService().merge(chatContext.getUser());
        botUtils.confirmCallback(chatContext.getUpdate().getCallbackQuery().getId());
    }

    public static List<InlineKeyboardButton> createControlButtons(int seriesId, int pagesCount, int page) {
        InlineKeyboardButton previous;
        InlineKeyboardButton next;

        if (page == 0) {
            previous = InlineKeyboardButton.builder()
                    .callbackData("NaN")
                    .text("×").build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(SeriesCallback.toJson(seriesId, page - 1).toString())
                    .text("‹").build();
        }

        if (pagesCount == page + 1) {
            next = InlineKeyboardButton.builder()
                    .callbackData("NaN")
                    .text("×").build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData(SeriesCallback.toJson(seriesId, page + 1).toString())
                    .text("›").build();
        }

        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData("NaN")
                .text(page + 1 + "/" + pagesCount).build();

        return Arrays.asList(previous, current, next);
    }
}