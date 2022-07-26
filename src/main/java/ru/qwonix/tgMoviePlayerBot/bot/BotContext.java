package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.Data;
import ru.qwonix.tgMoviePlayerBot.database.dao.DaoContext;

@Data
public class BotContext {
    private final DaoContext daoContext;
    private final Bot bot;

    public BotContext(Bot bot) {
        daoContext = new DaoContext();
        this.bot = bot;
    }
}
