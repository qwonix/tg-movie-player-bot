package ru.qwonix.tgMoviePlayerBot.bot.callback;

import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.database.servie.SeriesService;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryCallback extends Callback {
    private final BotContext botContext;
    private final ChatContext chatContext;

    public QueryCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }


    public void handleCallback(String searchText) {
        User user = chatContext.getUser();
        BotUtils botUtils = new BotUtils(botContext);

        // TODO: 15-Jul-22 smart search for name

        SeriesService seriesService = botContext
                .getDaoContext()
                .getSeriesService();

        List<Series> serials = seriesService.findAllByNameLike(searchText);
        if (serials.isEmpty()) {
            botUtils.sendMarkdownText(user, "*Ничего не найдено :(* \n`Попробуйте изменить запрос`");
            return;
        }


        Map<String, String> keyboard = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (Series series : serials) {
            LocalDate episodePremiereReleaseDate = seriesService.findEpisodePremiereReleaseDate(series);
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

        InlineKeyboardMarkup callbackKeyboard = BotUtils.createTwoRowsCallbackKeyboard(keyboard);

        botUtils.sendMarkdownText(user, String.format("Поиск по запросу: `%s`", searchText));
        botUtils.sendMarkdownTextWithKeyBoard(user, sb.toString(), callbackKeyboard);

    }

    @Override
    public void handleCallback(JSONObject callbackData) {
        String searchText = callbackData.getString("query");

        handleCallback(searchText);
    }
}
