package ru.qwonix.tgMoviePlayerBot.bot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
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
    private static final String lockCharacter = "×";

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

    @Override
    public void handleCallback(JSONObject callbackData) {
        int seasonId = callbackData.getInt("id");
        int page = callbackData.getInt("page");

        SeasonService seasonService = botContext.getDatabaseContext().getSeasonService();
        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();

        Optional<Season> optionalSeason = seasonService.find(seasonId);

        if (optionalSeason.isPresent()) {
            Season season = optionalSeason.get();

            int episodesCount = episodeService.countAllBySeason(season);
            int limit = 3;
            int pagesCount = (int) Math.ceil(episodesCount / (double) limit);

            List<Episode> seasonEpisodes = episodeService.findAllBySeasonOrderByNumberWithLimitAndPage(season, limit, page);

            String text = createText(season, seasonEpisodes);

            Map<String, String> keyboard = new LinkedHashMap<>();
            for (Episode episode : seasonEpisodes) {
                JSONObject episodeCallback = EpisodeCallback.toJSON(episode.getId());
                keyboard.put("Серия " + episode.getNumber() + " «" + episode.getName() + "»", episodeCallback.toString());
            }

            List<List<InlineKeyboardButton>> inlineKeyboard = BotUtils.createOneRowCallbackKeyboard(keyboard);

            if (pagesCount > 1) {
                List<InlineKeyboardButton> controlButtons = createControlButtons(seasonId, pagesCount, page);
                inlineKeyboard.add(controlButtons);
            }

            new BotUtils(botContext).sendMarkdownTextWithKeyBoardAndPhoto(chatContext.getUser()
                    , text
                    , new InlineKeyboardMarkup(inlineKeyboard)
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
        } else {
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

    private List<InlineKeyboardButton> createControlButtons(int seasonId, int pagesCount, int page) {
        InlineKeyboardButton previous;
        InlineKeyboardButton next;

        if (page == 0) {
            previous = InlineKeyboardButton.builder()
                    .callbackData(SeasonCallback.toJson(seasonId, page).toString())
                    .text(lockCharacter).build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(SeasonCallback.toJson(seasonId, page - 1).toString())
                    .text("‹").build();
        }

        if (pagesCount == page + 1) {
            next = InlineKeyboardButton.builder()
                    .callbackData(SeasonCallback.toJson(seasonId, page).toString())
                    .text(lockCharacter).build();
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
