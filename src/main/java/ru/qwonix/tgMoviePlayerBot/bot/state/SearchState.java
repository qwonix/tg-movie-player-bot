package ru.qwonix.tgMoviePlayerBot.bot.state;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SelectCallback;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchState extends UserState {

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

        List<Series> serials = botContext
                .getDaoContext()
                .getSeriesService()
                .findAllByNameLike(searchText);

        Map<String, String> keyboard = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        if (serials.isEmpty()) {
            botUtils.sendMarkdownText(user, "Ничего не найдено :\\(");
            return;
        }

        for (Series series : serials) {
            sb.append(String.format("`%s` *%s*", series.getName(), series.getCountry()));
            sb.append('\n');
            sb.append('\n');
            sb.append(String.format("_%s_", series.getDescription()));
            sb.append('\n');
            sb.append('\n');

            SelectCallback data
                    = new SelectCallback(SelectCallback.DataType.SERIES, series.getId());

            keyboard.put(series.getName(), data.toJSON().toString());
        }

        InlineKeyboardMarkup callbackKeyboard = BotUtils.createCallbackKeyboard(keyboard);

        botUtils.sendMarkdownText(user, String.format("Поиск по запросу: `%s`", searchText));

        botUtils.sendMarkdownTextWithKeyBoard(user, sb.toString(), callbackKeyboard);
    }

    @Override
    public void onVideo() {

    }

}
