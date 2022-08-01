package ru.qwonix.tgMoviePlayerBot.bot.state;

import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.callback.QueryCallback;
import ru.qwonix.tgMoviePlayerBot.entity.User;

public class SearchState extends State {

    public SearchState(ChatContext chatContext, BotContext botContext) {
        super(chatContext, botContext);
    }

    @Override
    public void onText() {
        String searchText = chatContext.getUpdate().getMessage().getText();
        new QueryCallback(botContext, chatContext).handleCallback(searchText);

        User user = chatContext.getUser();
        user.setStateType(StateType.DEFAULT);
        botContext.getDaoContext().getUserService().merge(user);
    }
}
