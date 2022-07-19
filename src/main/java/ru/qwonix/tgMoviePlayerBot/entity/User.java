package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;
import ru.qwonix.tgMoviePlayerBot.bot.state.UserState;


@Builder(toBuilder = true)
@Data
public class User {
    private long chatId;
    private String name;
    @Builder.Default
    private boolean isAdmin = false;
    @Builder.Default
    private UserState.State state = UserState.State.DEFAULT;
}
