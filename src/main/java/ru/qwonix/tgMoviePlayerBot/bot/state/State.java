package ru.qwonix.tgMoviePlayerBot.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.callback.Action;
import ru.qwonix.tgMoviePlayerBot.bot.callback.Callback;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SelectCallback;

@Slf4j
public abstract class State {
    protected final ChatContext chatContext;
    protected final BotContext botContext;

    public State(ChatContext chatContext, BotContext botContext) {
        this.chatContext = chatContext;
        this.botContext = botContext;
    }

    public static State getState(StateType stateType, ChatContext chatContext, BotContext botContext) {
        switch (stateType) {
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

        log.info("user {} callback {}", chatContext.getUser(), callbackData);

        JSONObject jsonObject = new JSONObject(callbackData);
        String actionStr = jsonObject.getString("action");

        JSONObject data = jsonObject.getJSONObject("data");
        Action action = Action.valueOf(actionStr);
        Callback callback = null;
        switch (action) {
            case SELECT:
                callback = new SelectCallback(data);
                break;
            case NEXT_PAGE:
                break;

            case PREVIOUS_PAGE:
                break;

            default:
                log.info("default in switch onCallback");
                return;

        }
        callback.handle(botContext, chatContext);
    }

    public enum StateType {
        DEFAULT, SEARCH
    }
}
