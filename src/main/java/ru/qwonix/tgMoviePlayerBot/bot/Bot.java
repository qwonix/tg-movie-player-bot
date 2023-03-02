package ru.qwonix.tgMoviePlayerBot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.qwonix.tgMoviePlayerBot.bot.state.DefaultState;
import ru.qwonix.tgMoviePlayerBot.bot.state.State;
import ru.qwonix.tgMoviePlayerBot.config.TelegramConfig;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.util.Optional;

public class Bot extends TelegramLongPollingBot {
    private final BotContext botContext;

    public Bot() {
        super(TelegramConfig.getProperty(TelegramConfig.BOT_TOKEN));
        botContext = new BotContext(this);
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

        Optional<User> optionalUser = botContext.getDatabaseContext().getUserService().
                findUser(telegramUser.getId());
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = User.builder()
                    .chatId(telegramUser.getId())
                    .name(telegramUser.getFirstName())
                    .build();
            botContext.getDatabaseContext().getUserService().merge(user);
        }

        if (update.hasCallbackQuery()) {
            this.onCallbackReceived(update, user);
            return;
        }

        if (!update.hasMessage()) {
            return;
        } else if (update.getMessage().hasText()) {
            this.onTextMessageReceived(update, user);
        } else if (update.getMessage().hasVideo()) {
            this.onVideoReceived(update, user);
        } else if (update.getMessage().hasPhoto()) {
            this.onPhotoReceived(update, user);
        }
    }

    private void onVideoReceived(Update update, User user) {
        ChatContext chatContext = new ChatContext(user, update);
        State state = State.getState(user.getStateType(), chatContext, botContext);

        state.onVideo();
    }

    private void onPhotoReceived(Update update, User user) {
        ChatContext chatContext = new ChatContext(user, update);
        State state = State.getState(user.getStateType(), chatContext, botContext);

        state.onPhoto();
    }

    private void onTextMessageReceived(Update update, User user) {
        ChatContext chatContext = new ChatContext(user, update);
        State state = State.getState(user.getStateType(), chatContext, botContext);

        String text = update.getMessage().getText();
        if (text.startsWith("/")) {
            state = new DefaultState(chatContext, botContext);
        }

        state.onText();
    }

    private void onCallbackReceived(Update update, User user) {
        ChatContext chatContext = new ChatContext(user, update);
        State state = State.getState(user.getStateType(), chatContext, botContext);

        state.onCallback();
    }

    @Override
    public String getBotUsername() {
        return TelegramConfig.getProperty(TelegramConfig.BOT_USERNAME);
    }
}