package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;
import ru.qwonix.tgMoviePlayerBot.bot.State;


@Builder(toBuilder = true)
@Data
public class User {
    private long chatId;
    private String name;
    @Builder.Default
    private boolean isAdmin = false;
    @Builder.Default
    private State state = State.DEFAULT;
}
