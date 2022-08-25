package ru.qwonix.tgMoviePlayerBot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.qwonix.tgMoviePlayerBot.bot.state.DefaultState;
import ru.qwonix.tgMoviePlayerBot.bot.state.State;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.config.TelegramConfig;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.util.Optional;

public class Bot extends TelegramLongPollingBot {
    private final BotContext botContext;

    public Bot() {
        this.botContext = new BotContext(this);
    }

    private static User convertTelegramUserToUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        return User.builder()
                .chatId(telegramUser.getId())
                .name(telegramUser.getFirstName())
                .messagesIds(new MessagesIds())
                .build();
    }

    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            this.onCallbackReceived(update);
            return;
        }

        if (!update.hasMessage()) {
            return;
        } else if (update.getMessage().hasText()) {
            this.onTextMessageReceived(update);
        } else if (update.getMessage().hasVideo()) {
            this.onVideoReceived(update);
        } else if (update.getMessage().hasPhoto()) {
            this.onPhotoReceived(update);
        }
    }

    private User findUserFromUpdate(Update update) {
        User user;
        org.telegram.telegrambots.meta.api.objects.User telegramUser;

        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            telegramUser = callbackQuery.getFrom();
        } else if (update.hasMessage()) {
            telegramUser = update.getMessage().getFrom();
        } else {
            throw new IllegalArgumentException("update has no user");
        }

        Optional<User> optionalUser = botContext.getDatabaseContext()
                .getUserService()
                .findUser(telegramUser.getId());

        user = optionalUser.orElseGet(() -> {
            User newUser = convertTelegramUserToUser(telegramUser);
            botContext.getDatabaseContext().getUserService().merge(newUser);
            return newUser;
        });
        return user;
    }

    private void onVideoReceived(Update update) {
        User user = findUserFromUpdate(update);
        ChatContext chatContext = new ChatContext(user, update);
        State state = State.getState(user.getStateType(), chatContext, botContext);

        state.onVideo();
    }

    private void onPhotoReceived(Update update) {
        User user = findUserFromUpdate(update);
        ChatContext chatContext = new ChatContext(user, update);
        State state = State.getState(user.getStateType(), chatContext, botContext);

        state.onPhoto();
    }

    private void onTextMessageReceived(Update update) {
        User user = findUserFromUpdate(update);
        ChatContext chatContext = new ChatContext(user, update);
        State state = State.getState(user.getStateType(), chatContext, botContext);

        String text = update.getMessage().getText();
        if (text.startsWith("/")) {
            state = new DefaultState(chatContext, botContext);
        }

        state.onText();
    }

    private void onCallbackReceived(Update update) {
        User user = findUserFromUpdate(update);
        ChatContext chatContext = new ChatContext(user, update);
        State state = State.getState(user.getStateType(), chatContext, botContext);

        state.onCallback();
    }


    @Override
    public String getBotUsername() {
        return TelegramConfig.getProperty(TelegramConfig.BOT_USERNAME);
    }

    @Override
    public String getBotToken() {
        return TelegramConfig.getProperty(TelegramConfig.BOT_TOKEN);
    }
}