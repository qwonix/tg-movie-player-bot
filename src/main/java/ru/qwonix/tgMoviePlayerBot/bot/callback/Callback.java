package ru.qwonix.tgMoviePlayerBot.bot.callback;

import org.json.JSONObject;

public abstract class Callback {
    public abstract void handleCallback(JSONObject callbackData);
}
