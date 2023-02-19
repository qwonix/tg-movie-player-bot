package ru.qwonix.tgMoviePlayerBot.callback;

import org.json.JSONObject;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.exception.*;

public abstract class Callback {
    public JSONObject toCallback(JSONObject callbackData) {
        JSONObject jsonCallback = new JSONObject();
//        jsonCallback.put("action", Action.SELECT);
        jsonCallback.put("data", callbackData);

        return jsonCallback;
    }

    public static JSONObject parseCallback(String callbackData) {
        JSONObject jsonCallback = new JSONObject(callbackData);

        return jsonCallback.getJSONObject("data");
    }

    public abstract void handleCallback(BotContext botContext, ChatContext chatContext) throws NoSuchEpisodeException, NoSuchSeasonException, NoSuchSeriesException, NoSuchVideoException, NoSuchMovieException;

    public abstract JSONObject toCallback();
}
