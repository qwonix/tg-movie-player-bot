package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;
import ru.qwonix.tgMoviePlayerBot.bot.state.State;

import java.util.Optional;


@Builder(toBuilder = true)
@Data
public class User {
    private long chatId;
    private String name;
    @Builder.Default
    private boolean isAdmin = false;
    @Builder.Default
    private State.StateType stateType = State.StateType.DEFAULT;
    private Integer messageIdToDelete;
}
