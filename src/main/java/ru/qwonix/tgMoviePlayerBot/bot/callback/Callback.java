package ru.qwonix.tgMoviePlayerBot.bot.callback;

import org.json.JSONObject;

public abstract class Callback {
    public static JSONObject toCallbackJson(JSONObject callbackData) {
        JSONObject jsonCallback = new JSONObject();
//        jsonCallback.put("action", Action.SELECT);
        jsonCallback.put("data", callbackData);

        return jsonCallback;
    }

    public static JSONObject fromCallbackJson(String callbackData) {
        JSONObject jsonCallback = new JSONObject(callbackData);

        return jsonCallback.getJSONObject("data");
    }

    public abstract void handleCallback(JSONObject callbackData);
}
