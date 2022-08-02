package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.Data;
import ru.qwonix.tgMoviePlayerBot.database.DatabaseContext;

@Data
public class BotContext {
    private final DatabaseContext databaseContext;
    private final Bot bot;

    public BotContext(Bot bot) {
        this.databaseContext = new DatabaseContext();
        this.bot = bot;
    }
}
