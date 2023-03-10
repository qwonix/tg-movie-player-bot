package ru.qwonix.tgMoviePlayerBot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.database.BasicConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeService;
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonService;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonServiceImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;
import ru.qwonix.tgMoviePlayerBot.entity.User;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchEpisodeException;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchSeasonException;

import java.util.*;

@Slf4j
public class SeasonCallback extends Callback {
    private final int seasonId;
    private final int page;

    private final SeasonService seasonService = new SeasonServiceImpl(BasicConnectionPool.getInstance());
    private final EpisodeService episodeService = new EpisodeServiceImpl(BasicConnectionPool.getInstance());


    public SeasonCallback(User user, int seasonId, int page, String callbackId) {
        super(user, callbackId);
        this.seasonId = seasonId;
        this.page = page;
    }

    public static JSONObject toJson(int seasonId, int page) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.SEASON);
        jsonData.put("id", seasonId);
        jsonData.put("page", page);

        return toCallback(jsonData);
    }

    @Override
    public void handle() throws NoSuchSeasonException, NoSuchEpisodeException {
        Optional<Season> optionalSeason = seasonService.find(seasonId);
        Season season;
        if (optionalSeason.isPresent()) {
            season = optionalSeason.get();
        } else {
            throw new NoSuchSeasonException("Такого сезона не существует. Попробуйте найти его заново.");
        }


        int totalEpisodesCountInSeason = episodeService.countAllBySeason(season);
        int keyboardPageEpisodesLimit = Integer.parseInt(BotConfig.getProperty(BotConfig.KEYBOARD_PAGE_EPISODES_MAX));
        int pagesCount = (int) Math.ceil(totalEpisodesCountInSeason / (double) keyboardPageEpisodesLimit);

        List<Episode> seasonEpisodes
                = episodeService.findAllBySeasonOrderByNumberWithLimitAndPage(season, keyboardPageEpisodesLimit, page);

        InlineKeyboardMarkup keyboard;
        if (seasonEpisodes.isEmpty()) {
            throw new NoSuchEpisodeException("В сезоне отсутствуют серии");
        } else {
            keyboard = createControlButtons(season.getId(), seasonEpisodes, page, pagesCount);
        }

        String text = createText(season, totalEpisodesCountInSeason);

        MessagesIds messagesIds = user.getMessagesIds();
        if (messagesIds.hasEpisodeMessageId()) {
            botUtils.deleteMessage(user, messagesIds.getEpisodeMessageId());
            messagesIds.setEpisodeMessageId(null);
        }
        if (messagesIds.hasVideoMessageId()) {
            botUtils.deleteMessage(user, messagesIds.getVideoMessageId());
            messagesIds.setVideoMessageId(null);
        }

        if (messagesIds.hasSeasonMessageId()) {
            botUtils.editPhotoWithMarkdownTextAndKeyboard(user
                    , messagesIds.getSeasonMessageId()
                    , text
                    , season.getPreviewTgFileId()
                    , keyboard);

        } else {
            Integer seriesMessageId = botUtils.sendPhotoWithMarkdownTextAndKeyboard(user
                    , text
                    , season.getPreviewTgFileId()
                    , keyboard);

            messagesIds.setSeasonMessageId(seriesMessageId);
        }
        userService.merge(user);
    }

    private static String createText(Season season, int episodesCount) {
        return String.format("*%s –* `%s сезон`\n", season.getSeries().getTitle(), season.getNumber())
                + '\n'
                + String.format("_%s_\n", season.getDescription())
                + '\n'
                + String.format("*Количество эпизодов*: `%d` / *%s*\n", episodesCount, season.getTotalEpisodesCount())
                + String.format("Премьера: `%s`\n", season.getFormattedPremiereReleaseDate())
                + String.format("Финал: `%s`\n", season.getFormattedFinalReleaseDate());
    }

    private static InlineKeyboardMarkup createControlButtons(int seasonId, List<Episode> seasonEpisodes, int page, int pagesCount) {
        Map<String, String> keyboardMap = new LinkedHashMap<>();
        for (Episode episode : seasonEpisodes) {
            JSONObject episodeCallback = EpisodeCallback.toJson(episode.getId());
            keyboardMap.put(episode.getSeason().getNumber() + "×" + episode.getNumber() + " «" + episode.getTitle() + "»", episodeCallback.toString());
        }

        List<List<InlineKeyboardButton>> inlineKeyboard = BotUtils.createTwoRowsCallbackKeyboard(keyboardMap);

        if (pagesCount > 1) {
            InlineKeyboardButton previous;
            InlineKeyboardButton next;

            if (page == 0) {
                previous = InlineKeyboardButton.builder()
                        .callbackData(EmptyCallback.toJson().toString())
                        .text("×").build();
            } else {
                previous = InlineKeyboardButton.builder()
                        .callbackData(SeasonCallback.toJson(seasonId, page - 1).toString())
                        .text("‹").build();
            }

            if (pagesCount == page + 1) {
                next = InlineKeyboardButton.builder()
                        .callbackData(EmptyCallback.toJson().toString())
                        .text("×").build();
            } else {
                next = InlineKeyboardButton.builder()
                        .callbackData(SeasonCallback.toJson(seasonId, page + 1).toString())
                        .text("›").build();
            }

            InlineKeyboardButton current = InlineKeyboardButton.builder()
                    .callbackData(EmptyCallback.toJson().toString())
                    .text(page + 1 + "/" + pagesCount).build();


            List<InlineKeyboardButton> controlButtons = Arrays.asList(previous, current, next);
            inlineKeyboard.add(controlButtons);
        }

        return new InlineKeyboardMarkup(inlineKeyboard);
    }
}
