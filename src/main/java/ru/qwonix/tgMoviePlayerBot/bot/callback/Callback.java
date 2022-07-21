package ru.qwonix.tgMoviePlayerBot.bot.callback;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Callback {
    public static String convertCallback(Action action, Object object) {
        Gson gson = new Gson();
        JsonElement jsonObject = gson.toJsonTree(object);

        JsonObject dataWrapper = new JsonObject();
        dataWrapper.addProperty("action", action.toString());
        dataWrapper.add("data", jsonObject);

        return dataWrapper.toString();
    }
}
