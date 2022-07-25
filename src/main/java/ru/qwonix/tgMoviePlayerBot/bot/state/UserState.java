package ru.qwonix.tgMoviePlayerBot.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.callback.Action;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SelectCallback;
import ru.qwonix.tgMoviePlayerBot.entity.User;

@Slf4j
public abstract class UserState {
    protected final ChatContext chatContext;
    protected final BotContext botContext;

    public UserState(ChatContext chatContext, BotContext botContext) {
        this.chatContext = chatContext;
        this.botContext = botContext;
    }

    public static UserState getState(State state, ChatContext chatContext, BotContext botContext) {
        switch (state) {
            case SEARCH:
                return new SearchState(chatContext, botContext);
            case DEFAULT:
            default:
                return new DefaultState(chatContext, botContext);
        }
    }

    public abstract void onText();

    public abstract void onVideo();

    public void onCallback() {
        CallbackQuery callbackQuery = chatContext.getUpdate().getCallbackQuery();
        String callbackData = callbackQuery.getData();

        JSONObject jsonObject = new JSONObject(callbackData);
        String actionStr = jsonObject.getString("action");

        JSONObject data = jsonObject.getJSONObject("data");
        Action action = Action.valueOf(actionStr);
        switch (action) {
            case SELECT:
                new SelectCallback(data).action(botContext, chatContext);
            case NEXT_PAGE:

            case PREVIOUS_PAGE:
        }

        User user = chatContext.getUser();
        log.info("user {} callback {}", user, data);
    }

    public enum State {
        DEFAULT, SEARCH
    }
}
