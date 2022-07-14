package ru.qwonix.tgMoviePlayerBot.user;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class User {
    private long chatId;
    private String name;
    @Builder.Default
    private boolean isAdmin = false;
}
