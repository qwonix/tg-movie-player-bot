package ru.qwonix.tgMoviePlayerBot.bot.callback;

import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;

import java.util.Arrays;
import java.util.List;

public class QueryCallback extends Callback {

    private final BotContext botContext;
    private final ChatContext chatContext;

    public QueryCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }

    public static JSONObject toJson(String query, int page) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.QUERY);
        jsonData.put("query", query);
        jsonData.put("page", page);

        return Callback.toCallbackJson(jsonData);
    }

    public static List<InlineKeyboardButton> createControlButtons(String query, int pagesCount, int page) {
        InlineKeyboardButton previous;
        InlineKeyboardButton next;

        if (page == 0) {
            previous = InlineKeyboardButton.builder()
                    .callbackData(QueryCallback.toJson(query, page).toString())
                    .text("×").build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(QueryCallback.toJson(query, page - 1).toString())
                    .text("‹").build();
        }

        if (pagesCount == page + 1) {
            next = InlineKeyboardButton.builder()
                    .callbackData(QueryCallback.toJson(query, page).toString())
                    .text("×").build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData(QueryCallback.toJson(query, page + 1).toString())
                    .text("›").build();
        }

        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData(QueryCallback.toJson(query, page).toString())
                .text(page + 1 + "/" + pagesCount).build();

        return Arrays.asList(previous, current, next);
    }

    public void handleCallback(String query, int page) {
        AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                .callbackQueryId(chatContext.getUpdate().getCallbackQuery().getId())
                .text("Поиск по названию находится в разработке :(")
                .showAlert(false)
                .build();

        new BotUtils(botContext).executeAlert(answerCallbackQuery);

        /*
        BotUtils botUtils = new BotUtils(botContext);

        SeriesService seriesService = botContext.getDatabaseContext().getSeriesService();
        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();

        int searchResultCount = seriesService.countAllByNameLike(query);
        if (searchResultCount == 0) {
            botUtils.sendMarkdownText(chatContext.getUser(), "*Ничего не найдено :(* \n`Попробуйте изменить запрос`");
            return;
        }

        int limit = 3;
        int pagesCount = (int) Math.ceil(searchResultCount / (double) limit);

        Map<String, String> keyboard = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        List<Series> serials = seriesService.findAllByNameLikeWithLimitAndPage(query, limit, page);
        for (Series series : serials) {
            LocalDate premiereReleaseDate = episodeService.findPremiereReleaseDate(series);
            sb.append(String.format("`%s` – *%s* (%s)\n", series.getName(), series.getCountry(), premiereReleaseDate.getYear()));
            sb.append('\n');
            String description = series.getDescription()
                    .substring(0, series.getDescription().indexOf(' ', 90))
                    + "...";
            sb.append(String.format("_%s_\n", description));
            sb.append('\n');

            JSONObject seriesCallback = SeriesCallback.toJson(series.getId(), 0);
            keyboard.put(series.getName() + " (" + premiereReleaseDate.getYear() + ")", seriesCallback.toString());
        }

        List<List<InlineKeyboardButton>> inlineKeyboard = BotUtils.createOneRowCallbackKeyboard(keyboard);

        if (pagesCount > 1) {
            List<InlineKeyboardButton> controlButtons = createControlButtons(query, pagesCount, page);
            inlineKeyboard.add(controlButtons);
        }

        botUtils.sendMarkdownTextWithKeyBoard(chatContext.getUser(), sb.toString(), new InlineKeyboardMarkup(inlineKeyboard));
         */
    }

    @Override
    public void handleCallback(JSONObject callbackData) {
        String query = callbackData.getString("query");
        int page = callbackData.getInt("page");
        handleCallback(query, page);
    }
}
