package ru.qwonix.tgMoviePlayerBot.bot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeService;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonService;
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

    public static JSONObject toJson(int seasonId, int page) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.SEASON);
        jsonData.put("id", seasonId);
        jsonData.put("page", page);

        return Callback.toCallbackJson(jsonData);
    }


    public void handleCallback(int seasonId, int page) {
        SeasonService seasonService = botContext.getDatabaseContext().getSeasonService();
        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();

        Optional<Season> optionalSeason = seasonService.find(seasonId);

        if (optionalSeason.isPresent()) {
            Season season = optionalSeason.get();

            int episodesCount = episodeService.countAllBySeason(season);
            int limit = 12;
            int pagesCount = (int) Math.ceil(episodesCount / (double) limit);

            List<Episode> seasonEpisodes = episodeService.findAllBySeasonOrderByNumberWithLimitAndPage(season, limit, page);

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
            } else {
                seriesFinalReleaseDate = season.getFinalReleaseDate()
                        .format(DateTimeFormatter.ofPattern("d MMMM y", Locale.forLanguageTag("ru")));
            }

            String text = String.format("*%s – %s сезон*\n", season.getSeries().getName(), season.getNumber())
                    + '\n'
                    + String.format("_%s_\n", season.getDescription())
                    + '\n'
                    + String.format("*Количество эпизодов*: *%d* / *%s*\n", episodesCount, season.getTotalEpisodesCount())
                    + String.format("*Премьера: _%s_*\n", seriesPremiereReleaseDate)
                    + String.format("*Финал: _%s_*\n", seriesFinalReleaseDate);


            Map<String, String> keyboard = new LinkedHashMap<>();
            for (Episode episode : seasonEpisodes) {
                JSONObject episodeCallback = EpisodeCallback.toJSON(episode.getId());
                keyboard.put("Серия " + episode.getNumber() + " «" + episode.getTitle() + "»", episodeCallback.toString());
            }

            List<List<InlineKeyboardButton>> inlineKeyboard = BotUtils.createTwoRowsCallbackKeyboard(keyboard);

            if (pagesCount > 1) {
                List<InlineKeyboardButton> controlButtons = createControlButtons(seasonId, pagesCount, page);
                inlineKeyboard.add(controlButtons);
            }

            BotUtils botUtils = new BotUtils(botContext);
            MessagesIds messagesIds = chatContext.getUser().getMessagesIds();

//            if (messagesIds.hasEpisodeMessageId()) {
//                botUtils.deleteMessage(chatContext.getUser(), messagesIds.getEpisodeMessageId());
//                messagesIds.setEpisodeMessageId(null);
//            }
//            if (messagesIds.hasVideoMessageId()) {
//                botUtils.deleteMessage(chatContext.getUser(), messagesIds.getVideoMessageId());
//                messagesIds.setVideoMessageId(null);
//            }

            if (messagesIds.hasSeasonMessageId()) {
                botUtils.editMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                        , messagesIds.getSeasonMessageId()
                        , text
                        , new InlineKeyboardMarkup(inlineKeyboard)
                        , season.getPreviewFileId());

            } else {
                Integer seriesMessageId = botUtils.sendMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                        , text
                        , new InlineKeyboardMarkup(inlineKeyboard)
                        , season.getPreviewFileId());
                messagesIds.setSeasonMessageId(seriesMessageId);

            }
            botContext.getDatabaseContext().getUserService().merge(chatContext.getUser());

        } else {
            String text = "Такого сезона не существует. `Попробуйте изменить запрос`.";
            log.error("no season with {} id", seasonId);
            new BotUtils(botContext).sendMarkdownText(chatContext.getUser(), text);
        }
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
                    .callbackData(SeasonCallback.toJson(seasonId, page).toString())
                    .text("×").build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(SeasonCallback.toJson(seasonId, page - 1).toString())
                    .text("‹").build();
        }

        if (pagesCount == page + 1) {
            next = InlineKeyboardButton.builder()
                    .callbackData(SeasonCallback.toJson(seasonId, page).toString())
                    .text("×").build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData(SeasonCallback.toJson(seasonId, page + 1).toString())
                    .text("›").build();
        }

        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData(SeasonCallback.toJson(seasonId, page).toString())
                .text(page + 1 + "/" + pagesCount).build();

        return Arrays.asList(previous, current, next);
    }
}
