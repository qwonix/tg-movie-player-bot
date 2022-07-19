package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.qwonix.tgMoviePlayerBot.entity.User;

@Data
public class ChatContext {
    private final User user;
    private final Update update;

    public ChatContext(User user, Update update) {
        this.user = user;
        this.update = update;
    }
}
