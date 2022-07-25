package ru.qwonix.tgMoviePlayerBot.bot.callback;

import org.json.JSONObject;

public class Callback {
    public static String convertCallback(Action action, SelectCallback.DataType callbackType, int id) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", callbackType.name());
        jsonData.put("id", id);

        JSONObject jsonCallback = new JSONObject();
        jsonCallback.put("action", action.toString());
        jsonCallback.put("data", jsonData);

        return jsonCallback.toString();
    }
}
