package ru.qwonix.tgMoviePlayerBot.bot.state;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.qwonix.tgMoviePlayerBot.bot.Bot;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.entity.User;

public class SearchState extends State {

    public SearchState(User user, Update update) {
        super(user, update);
    }

    @Override
    public void onText() {
        String query = update.getMessage().getText();
        botUtils.sendMarkdownText(user, String.format("Поиск по запросу: `%s`", query));
        user.setStateType(StateType.DEFAULT);
        userService.merge(user);
    }
}
