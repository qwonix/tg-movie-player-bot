package ru.qwonix.tgMoviePlayerBot.entity;

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
