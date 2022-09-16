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
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeService;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.Season;

import java.util.*;

@Slf4j
public class SeasonCallback extends Callback {
    private final BotContext botContext;
    private final ChatContext chatContext;

    public SeasonCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }

    public static JSONObject toJson(int seasonId, int page) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.SEASON);
        jsonData.put("id", seasonId);
        jsonData.put("page", page);

        return Callback.toCallbackJson(jsonData);
    }


    public void handleCallback(int seasonId, int page) {
        Optional<Season> optionalSeason = botContext.getDatabaseContext().getSeasonService().find(seasonId);

        if (optionalSeason.isPresent()) {
            this.onSeasonExists(optionalSeason.get(), page);

        } else {
            new BotUtils(botContext).executeAlertWithText(chatContext.getUpdate().getCallbackQuery().getId()
                    , "Такого сезона не существует. Попробуйте найти его заново."
                    , false);

            log.error("no season with {} id", seasonId);
        }
    }

    private void onSeasonExists(Season season, int page) {
        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();
        BotUtils botUtils = new BotUtils(botContext);

        int episodesCount = episodeService.countAllBySeason(season);
        int limit = Integer.parseInt(BotConfig.getProperty(BotConfig.KEYBOARD_PAGE_EPISODES_MAX));
        int pagesCount = (int) Math.ceil(episodesCount / (double) limit);

        InlineKeyboardMarkup keyboard;
        List<Episode> seasonEpisodes = episodeService.findAllBySeasonOrderByNumberWithLimitAndPage(season, limit, page);
        if (seasonEpisodes.isEmpty()) {
            botUtils.executeAlertWithText(chatContext.getUpdate().getCallbackQuery().getId()
                    , "Информации о сериях нет"
                    , true);
            keyboard = new InlineKeyboardMarkup(Collections.emptyList());
        } else {
            keyboard = this.generateKeyboard(season, seasonEpisodes, page, pagesCount);
        }

        String text = String.format("*%s –* `%s сезон`\n", season.getSeries().getName(), season.getNumber())
                + '\n'
                + String.format("_%s_\n", season.getDescription())
                + '\n'
                + String.format("*Количество эпизодов*: `%d` / *%s*\n", episodesCount, season.getTotalEpisodesCount())
                + String.format("*Премьера:* `%s`\n", season.getFormattedPremiereReleaseDate())
                + String.format("*Финал:* `%s`\n", season.getFormattedFinalReleaseDate());

        MessagesIds messagesIds = chatContext.getUser().getMessagesIds();

        if (messagesIds.hasSeasonMessageId()) {
            botUtils.editMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                    , messagesIds.getSeasonMessageId()
                    , text
                    , keyboard
                    , season.getPreviewFileId());

        } else {
            Integer seriesMessageId = botUtils.sendMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                    , text
                    , keyboard
                    , season.getPreviewFileId());

            messagesIds.setSeasonMessageId(seriesMessageId);
        }
        botContext.getDatabaseContext().getUserService().merge(chatContext.getUser());
        botUtils.confirmCallback(chatContext.getUpdate().getCallbackQuery().getId());
    }

    private InlineKeyboardMarkup generateKeyboard(Season season, List<Episode> seasonEpisodes, int page, int pagesCount) {
        InlineKeyboardMarkup keyboard;
        Map<String, String> keyboardMap = new LinkedHashMap<>();
        for (Episode episode : seasonEpisodes) {
            JSONObject episodeCallback = EpisodeCallback.toJSON(episode.getId());
            keyboardMap.put(episode.getSeason().getNumber() + "×" + episode.getNumber() + " «" + episode.getTitle() + "»", episodeCallback.toString());
        }

        List<List<InlineKeyboardButton>> inlineKeyboard = BotUtils.createTwoRowsCallbackKeyboard(keyboardMap);
        if (pagesCount > 1) {
            List<InlineKeyboardButton> controlButtons = createControlButtons(season.getId(), pagesCount, page);
            inlineKeyboard.add(controlButtons);
        }
        keyboard = new InlineKeyboardMarkup(inlineKeyboard);
        return keyboard;
    }

    @Override
    public void handleCallback(JSONObject callbackData) {
        int seasonId = callbackData.getInt("id");
        int page = callbackData.getInt("page");

        handleCallback(seasonId, page);
    }


    private List<InlineKeyboardButton> createControlButtons(int seasonId, int pagesCount, int page) {
        InlineKeyboardButton previous;
        InlineKeyboardButton next;

        if (page == 0) {
            previous = InlineKeyboardButton.builder()
                    .callbackData("NaN")
                    .text("×").build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(SeasonCallback.toJson(seasonId, page - 1).toString())
                    .text("‹").build();
        }

        if (pagesCount == page + 1) {
            next = InlineKeyboardButton.builder()
                    .callbackData("NaN")
                    .text("×").build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData(SeasonCallback.toJson(seasonId, page + 1).toString())
                    .text("›").build();
        }

        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData("NaN")
                .text(page + 1 + "/" + pagesCount).build();

        return Arrays.asList(previous, current, next);
    }
}
