package ru.qwonix.tgMoviePlayerBot.database.service.user;

import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.util.Optional;

public interface UserService {
    User setAdmin(User user);

    boolean exists(long chatId);

    void merge(User user);

    Optional<User> findUser(long userChatId);
}
