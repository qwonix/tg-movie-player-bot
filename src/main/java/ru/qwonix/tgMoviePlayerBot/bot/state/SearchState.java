package ru.qwonix.tgMoviePlayerBot.bot.state;

import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.entity.User;

public class SearchState extends State {

    public SearchState(ChatContext chatContext, BotContext botContext) {
        super(chatContext, botContext);
    }

    @Override
    public void onText() {
        String query = chatContext.getUpdate().getMessage().getText();
        new BotUtils(botContext).sendMarkdownText(chatContext.getUser(), String.format("Поиск по запросу: `%s`", query));

        User user = chatContext.getUser();
        user.setStateType(StateType.DEFAULT);
        botContext.getDatabaseContext().getUserService().merge(user);
    }
}
