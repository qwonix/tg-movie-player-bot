package ru.qwonix.tgMoviePlayerBot.bot.callback;

import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeService;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesService;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryCallback extends Callback {
    private static final String lockCharacter = "×";

    private final BotContext botContext;
    private final ChatContext chatContext;

    public QueryCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }

    public static JSONObject toJson(String query, int offset) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.QUERY);
        jsonData.put("query", query);
        jsonData.put("offset", offset);

        return Callback.toCallbackJson(jsonData);
    }

    public void handleCallback(String query, int offset) {
        User user = chatContext.getUser();
        BotUtils botUtils = new BotUtils(botContext);

        SeriesService seriesService = botContext.getDatabaseContext().getSeriesService();
        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();

        int searchResultCount = seriesService.countAllByNameLike(query);
        if (searchResultCount == 0) {
            botUtils.sendMarkdownText(user, "*Ничего не найдено :(* \n`Попробуйте изменить запрос`");
            return;
        }

        int limit = 3;
        int pagesCount = (int) Math.ceil(searchResultCount / (double) limit);

        Map<String, String> keyboard = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        List<Series> serials = seriesService.findAllByNameLikeWithLimitAndOffset(query, limit, offset);
        for (Series series : serials) {
            LocalDate episodePremiereReleaseDate = episodeService.findEpisodePremiereReleaseDate(series);
            sb.append(String.format("`%s` – *%s* (%s)\n", series.getName(), series.getCountry(), episodePremiereReleaseDate.getYear()));
            sb.append('\n');
            String description = series.getDescription()
                    .substring(0, series.getDescription().indexOf(' ', 90))
                    + "...";
            sb.append(String.format("_%s_\n", description));
            sb.append('\n');

            JSONObject seriesCallback = SeriesCallback.toJson(series.getId());
            keyboard.put(series.getName() + " (" + episodePremiereReleaseDate.getYear() + ")", seriesCallback.toString());
        }

        List<List<InlineKeyboardButton>> inlineKeyboard = BotUtils.createOneRowCallbackKeyboard(keyboard);

        if (pagesCount > 1) {
            List<InlineKeyboardButton> controlButtons = createControlButtons(query, pagesCount, offset);
            inlineKeyboard.add(controlButtons);
        }

        botUtils.sendMarkdownTextWithKeyBoard(user, sb.toString(), new InlineKeyboardMarkup(inlineKeyboard));
    }

    private List<InlineKeyboardButton> createControlButtons(String query, int pagesCount, int offset) {
        InlineKeyboardButton previous;
        InlineKeyboardButton next;

        if (offset == 0) {
            previous = InlineKeyboardButton.builder()
                    .callbackData(QueryCallback.toJson(query, offset).toString())
                    .text(lockCharacter).build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(QueryCallback.toJson(query, offset - 1).toString())
                    .text("‹").build();
        }

        if (pagesCount == offset + 1) {
            next = InlineKeyboardButton.builder()
                    .callbackData(QueryCallback.toJson(query, offset).toString())
                    .text(lockCharacter).build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData(QueryCallback.toJson(query, offset + 1).toString())
                    .text("›").build();
        }

        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData(QueryCallback.toJson(query, offset).toString())
                .text(offset + 1 + "/" + pagesCount).build();

        return Arrays.asList(previous, current, next);
    }

    @Override
    public void handleCallback(JSONObject callbackData) {
        String query = callbackData.getString("query");
        int offset = callbackData.getInt("offset");
        handleCallback(query, offset);
    }
}
