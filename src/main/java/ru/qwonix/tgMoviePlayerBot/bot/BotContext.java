package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.qwonix.tgMoviePlayerBot.dao.DaoContext;
import ru.qwonix.tgMoviePlayerBot.entity.User;

@AllArgsConstructor
@Getter
public class BotContext {
    private User user;
    private Update update;
    private DaoContext daoContext;
    private BotFeatures botFeatures;
    private BotCommand botCommand;
}
