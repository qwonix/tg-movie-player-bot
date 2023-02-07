package ru.qwonix.tgMoviePlayerBot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.database.DatabaseContext;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchEpisodeException;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchSeriesException;

import java.util.*;

@Slf4j
public class SeriesCallback extends Callback {
    private final int seriesId;
    private final int page;

    public SeriesCallback(int seriesId, int page) {
        this.seriesId = seriesId;
        this.page = page;
    }

    public SeriesCallback(JSONObject callbackData) {
        this.seriesId = callbackData.getInt("id");
        this.page = callbackData.getInt("page");
    }

    @Override
    public JSONObject toCallback() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.SERIES);
        jsonData.put("id", seriesId);
        jsonData.put("page", page);

        return toCallback(jsonData);
    }

    @Override
    public void handleCallback(BotContext botContext, ChatContext chatContext) throws NoSuchSeriesException, NoSuchEpisodeException {
        BotUtils botUtils = new BotUtils(botContext);

        DatabaseContext databaseContext = botContext.getDatabaseContext();
        Optional<Series> optionalSeries = databaseContext.getSeriesService().find(seriesId);
        Series series;
        if (optionalSeries.isPresent()) {
            series = optionalSeries.get();
        } else {
            throw new NoSuchSeriesException("Такого сериала не существует. Попробуйте найти его заново.");
        }

        int seasonsCount = databaseContext.getSeasonService().countAllBySeries(series);
        int keyboardPageSeasonsLimit = Integer.parseInt(BotConfig.getProperty(BotConfig.KEYBOARD_PAGE_SEASONS_MAX));
        int pagesCount = (int) Math.ceil(seasonsCount / (double) keyboardPageSeasonsLimit);

        List<Season> seriesSeasons
                = databaseContext.getSeasonService().findAllBySeriesOrderByNumberWithLimitAndPage(series, keyboardPageSeasonsLimit, page);

        InlineKeyboardMarkup keyboard;
        if (seriesSeasons.isEmpty()) {
            throw new NoSuchEpisodeException("В сериале отсутствуют серии");
        } else {
            Map<String, String> keyboardMap = new LinkedHashMap<>();
            for (Season season : seriesSeasons) {
                JSONObject callbackSeason = new SeasonCallback(season, 0).toCallback();
                keyboardMap.put("Сезон " + season.getNumber(), callbackSeason.toString());
            }
            List<List<InlineKeyboardButton>> inlineKeyboard = BotUtils.createTwoRowsCallbackKeyboard(keyboardMap);

            if (pagesCount > 1) {
                List<InlineKeyboardButton> controlButtons = createControlButtons(series.getId(), pagesCount, page);
                inlineKeyboard.add(controlButtons);
            }
            keyboard = new InlineKeyboardMarkup(inlineKeyboard);
        }

        String text = createText(series);

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

        databaseContext.getUserService().merge(chatContext.getUser());
        botUtils.confirmCallback(chatContext.getUpdate().getCallbackQuery().getId());
    }

    private static String createText(Series series) {
        return String.format("*%s*\n", series.getName())
                + '\n'
                + String.format("_%s_", series.getDescription());
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
                    .callbackData(new SeriesCallback(seriesId, page - 1).toCallback().toString())
                    .text("‹").build();
        }

        if (pagesCount == page + 1) {
            next = InlineKeyboardButton.builder()
                    .callbackData("NaN")
                    .text("×").build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData(new SeriesCallback(seriesId, page + 1).toCallback().toString())
                    .text("›").build();
        }

        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData("NaN")
                .text(page + 1 + "/" + pagesCount).build();

        return Arrays.asList(previous, current, next);
    }
}