package ru.qwonix.tgMoviePlayerBot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.entity.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.database.BasicConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonService;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesService;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesServiceImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.User;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchEpisodeException;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchSeriesException;

import java.util.*;

@Slf4j
public class SeriesCallback extends Callback {
    private final int seriesId;
    private final int page;

    private final SeriesService seriesService = new SeriesServiceImpl(BasicConnectionPool.getInstance());
    private final SeasonService seasonService = new SeasonServiceImpl(BasicConnectionPool.getInstance());

    public SeriesCallback(User user, int seriesId, int page, String callbackId) {
        super(user, callbackId);
        this.seriesId = seriesId;
        this.page = page;
    }

    public static JSONObject toJson(int seriesId, int page) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.SERIES);
        jsonData.put("id", seriesId);
        jsonData.put("page", page);

        return toCallback(jsonData);
    }

    @Override
    public void handle() throws NoSuchSeriesException, NoSuchEpisodeException {
        Optional<Series> optionalSeries = seriesService.find(seriesId);
        Series series;
        if (optionalSeries.isPresent()) {
            series = optionalSeries.get();
        } else {
            throw new NoSuchSeriesException("Такого сериала не существует. Попробуйте найти его заново.");
        }

        int seasonsCount = seasonService.countAllBySeries(series);
        int keyboardPageSeasonsLimit = Integer.parseInt(BotConfig.getProperty(BotConfig.KEYBOARD_PAGE_SEASONS_MAX));
        int pagesCount = (int) Math.ceil(seasonsCount / (double) keyboardPageSeasonsLimit);

        List<Season> seriesSeasons
                = seasonService.findAllBySeriesOrderByNumberWithLimitAndPage(series, keyboardPageSeasonsLimit, page);

        InlineKeyboardMarkup keyboard;
        if (seriesSeasons.isEmpty()) {
            throw new NoSuchEpisodeException("В сериале отсутствуют серии");
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

        String text = createText(series);

        MessagesIds messagesIds = user.getMessagesIds();
        if (messagesIds.hasSeasonMessageId()) {
            botUtils.deleteMessage(user, messagesIds.getSeasonMessageId());
            messagesIds.setSeasonMessageId(null);
        }
        if (messagesIds.hasEpisodeMessageId()) {
            botUtils.deleteMessage(user, messagesIds.getEpisodeMessageId());
            messagesIds.setEpisodeMessageId(null);
        }
        if (messagesIds.hasVideoMessageId()) {
            botUtils.deleteMessage(user, messagesIds.getVideoMessageId());
            messagesIds.setVideoMessageId(null);
        }

        if (messagesIds.hasSeriesMessageId()) {
            botUtils.editPhotoWithKeyboard(user
                    , messagesIds.getSeriesMessageId()
                    , keyboard
                    , series.getPreviewTgFileId());
        } else {
            Integer seriesMessageId = botUtils.sendPhotoWithMarkdownTextAndKeyboard(user
                    , text
                    , series.getPreviewTgFileId()
                    , keyboard);

            messagesIds.setSeriesMessageId(seriesMessageId);
        }

        userService.merge(user);
    }

    private static String createText(Series series) {
        return String.format("*%s*\n", series.getTitle())
                + '\n'
                + String.format("_%s_", series.getDescription());
    }

    public static List<InlineKeyboardButton> createControlButtons(int seriesId, int pagesCount, int page) {
        InlineKeyboardButton previous;
        InlineKeyboardButton next;

        if (page == 0) {
            previous = InlineKeyboardButton.builder()
                    .callbackData(EmptyCallback.toJson().toString())
                    .text("×").build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(SeriesCallback.toJson(seriesId, page - 1).toString())
                    .text("‹").build();
        }

        if (pagesCount == page + 1) {
            next = InlineKeyboardButton.builder()
                    .callbackData(EmptyCallback.toJson().toString())
                    .text("×").build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData(SeriesCallback.toJson(seriesId, page + 1).toString())
                    .text("›").build();
        }

        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData(EmptyCallback.toJson().toString())
                .text(page + 1 + "/" + pagesCount).build();

        return Arrays.asList(previous, current, next);
    }
}