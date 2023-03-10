package ru.qwonix.tgMoviePlayerBot.callback;

import org.json.JSONObject;
import ru.qwonix.tgMoviePlayerBot.bot.Bot;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.database.BasicConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserService;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserServiceImpl;
import ru.qwonix.tgMoviePlayerBot.entity.User;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchCallbackException;

public abstract class Callback {
    public enum DataType {
        VIDEO, EPISODE, SEASON, SERIES, MOVIE, QUERY, EMPTY
    }

    protected final User user;
    private final String callbackId;
    protected final BotUtils botUtils = new BotUtils(Bot.getInstance());
    protected final UserService userService = new UserServiceImpl(BasicConnectionPool.getInstance());

    public Callback(User user, String callbackId) {
        this.user = user;
        this.callbackId = callbackId;
    }

    public static JSONObject toCallback(JSONObject callbackData) {
        JSONObject jsonCallback = new JSONObject();
        jsonCallback.put("data", callbackData);

        return jsonCallback;
    }

    public static JSONObject parseCallback(String callbackData) {
        return new JSONObject(callbackData).getJSONObject("data");
    }

    public abstract void handle() throws NoSuchCallbackException;

    public void confirm() {
        if (callbackId != null)
            botUtils.executeAlert(callbackId, false);
    }
}
