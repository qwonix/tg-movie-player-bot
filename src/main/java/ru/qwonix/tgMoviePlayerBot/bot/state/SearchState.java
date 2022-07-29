package ru.qwonix.tgMoviePlayerBot.bot.state;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SelectCallback;
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

        Map<String, String> keyboard = new HashMap<>();
        if (serials.isEmpty()) {
            botUtils.sendMarkdownText(user, "*Ничего не найдено :(* \n`Попробуйте изменить запрос`");
            return;
        }

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

            SelectCallback data
                    = new SelectCallback(SelectCallback.DataType.SERIES, series.getId());

            keyboard.put(series.getName() + " (" + episodePremiereReleaseDate.getYear() + ")", data.toJSON().toString());
        }

        InlineKeyboardMarkup callbackKeyboard = BotUtils.createTwoRowsCallbackKeyboard(keyboard);

        botUtils.sendMarkdownText(user, String.format("Поиск по запросу: `%s`", searchText));
        botUtils.sendMarkdownTextWithKeyBoard(user, sb.toString(), callbackKeyboard);

        user.setStateType(StateType.DEFAULT);
        botContext.getDaoContext().getUserService().merge(user);
    }
}
