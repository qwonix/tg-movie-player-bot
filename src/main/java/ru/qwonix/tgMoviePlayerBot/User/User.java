package ru.qwonix.tgMoviePlayerBot.User;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class User {
    private long chatId;
    private String name;
    private boolean isAdmin;
}
