package ru.qwonix.tgMoviePlayerBot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.qwonix.tgMoviePlayerBot.bot.state.DefaultState;
import ru.qwonix.tgMoviePlayerBot.bot.state.SearchState;
import ru.qwonix.tgMoviePlayerBot.bot.state.State;
import ru.qwonix.tgMoviePlayerBot.config.TelegramConfig;
import ru.qwonix.tgMoviePlayerBot.database.BasicConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserService;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserServiceImpl;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.util.Optional;


public class Bot extends TelegramLongPollingBot {
    private static Bot INSTANCE;
    private final UserService userService = new UserServiceImpl(BasicConnectionPool.getInstance());

    private Bot() {
        super(TelegramConfig.getProperty(TelegramConfig.BOT_TOKEN));
    }

    public static Bot getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Bot();
        }

        return INSTANCE;
    }

    @Override
    public void onUpdateReceived(Update update) {
        org.telegram.telegrambots.meta.api.objects.User telegramUser;
        if (update.hasMyChatMember()) {
            throw new IllegalArgumentException("change status " + update.getMyChatMember().getFrom());
        }
        if (update.hasCallbackQuery()) {
            telegramUser = update.getCallbackQuery().getFrom();
        } else if (update.hasMessage()) {
            telegramUser = update.getMessage().getFrom();
        } else {
            throw new IllegalArgumentException("update has no user");
        }

        Optional<User> optionalUser = userService.findUser(telegramUser.getId());
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = User.builder()
                    .chatId(telegramUser.getId())
                    .name(telegramUser.getFirstName())
                    .build();
            userService.merge(user);
        }

        State state;
        switch (user.getStateType()) {
            case SEARCH:
                state = new SearchState(user, update);
                break;
            case DEFAULT:
                state = new DefaultState(user, update);
                break;
            default:
                state = new DefaultState(user, update);
                break;
        }

        if (update.hasCallbackQuery()) {
            state.onCallback();
        } else if (!update.hasMessage()) {
        } else if (update.getMessage().hasText()) {
            if (update.getMessage().getText().startsWith("/")) {
                new DefaultState(user, update).onText();
            }

        } else if (update.getMessage().hasVideo()) {
            state.onVideo();
        } else if (update.getMessage().hasPhoto()) {
            state.onPhoto();
        }
    }

    @Override
    public String getBotUsername() {
        return TelegramConfig.getProperty(TelegramConfig.BOT_USERNAME);
    }
}