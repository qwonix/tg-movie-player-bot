package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import ru.qwonix.tgMoviePlayerBot.bot.state.State;

@ToString
@Builder(toBuilder = true)
@Data
public class User {
    private long chatId;
    private String name;
    @Builder.Default
    private boolean isAdmin = false;
    @Builder.Default
    private State.StateType stateType = State.StateType.DEFAULT;
    @Builder.Default
    private MessagesIds messagesIds = new MessagesIds();
}
