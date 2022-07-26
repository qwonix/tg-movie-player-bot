package ru.qwonix.tgMoviePlayerBot.bot.callback;

import org.json.JSONObject;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;

public abstract class Callback {
    public abstract JSONObject toJSON();

    public abstract void handle(BotContext botContext, ChatContext chatContext);
}
