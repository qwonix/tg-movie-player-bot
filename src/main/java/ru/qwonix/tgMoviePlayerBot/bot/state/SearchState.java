package ru.qwonix.tgMoviePlayerBot.bot.state;

import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SeriesCallback;
import ru.qwonix.tgMoviePlayerBot.database.servie.SeriesService;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchState extends State {

    public SearchState(ChatContext chatContext, BotContext botContext) {
        super(chatContext, botContext);
    }

    @Override
    public void onText() {
        User user = chatContext.getUser();
        Update update = chatContext.getUpdate();
        BotUtils botUtils = new BotUtils(botContext);

        String searchText = update.getMessage().getText();

        // TODO: 15-Jul-22 smart search for name

        SeriesService seriesService = botContext
                .getDaoContext()
                .getSeriesService();

        List<Series> serials = seriesService.findAllByNameLike(searchText);
        if (serials.isEmpty()) {
            botUtils.sendMarkdownText(user, "*Ничего не найдено :(* \n`Попробуйте изменить запрос`");
            return;
        }

        messageCreation(searchText, serials);

        user.setStateType(StateType.DEFAULT);
        botContext.getDaoContext().getUserService().merge(user);
    }

    private void messageCreation(String searchText, List<Series> serials) {
        User user = chatContext.getUser();

        BotUtils botUtils = new BotUtils(botContext);
        Map<String, String> keyboard = new HashMap<>();
        SeriesService seriesService = botContext
                .getDaoContext()
                .getSeriesService();

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
}
