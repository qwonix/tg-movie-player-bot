package ru.qwonix.tgMoviePlayerBot.bot.state;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.callback.Action;
import ru.qwonix.tgMoviePlayerBot.bot.callback.Callback;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SelectCallback;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SelectCallbackType;
import ru.qwonix.tgMoviePlayerBot.dao.DaoContext;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
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
        String data = callbackQuery.getData();

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(data, JsonObject.class);
        Action action = Action.valueOf(jsonObject.get("action").getAsString());

        switch (action) {
            case SELECT:
                SelectCallback callback = new Gson().fromJson(jsonObject.getAsJsonObject("data"), SelectCallback.class);
                callback.action(botContext, chatContext);
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
