package ru.qwonix.tgMoviePlayerBot.Bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.qwonix.tgMoviePlayerBot.User.User;
import ru.qwonix.tgMoviePlayerBot.User.UserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class BotFeatures {

    private final Bot bot;
    private final UserDao userDAO;

    public BotFeatures(Bot bot, UserDao userDAO) {
        this.bot = bot;
        this.userDAO = userDAO;
    }

    public static InlineKeyboardMarkup createKeyboard(Map<String, String> buttons) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (Map.Entry<String, String> button : buttons.entrySet()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

            inlineKeyboardButton.setText(button.getKey());
            inlineKeyboardButton.setUrl(button.getValue());
            rowInline.add(inlineKeyboardButton);
        }
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public void sendText(User user, String text) {
        SendMessage message = new SendMessage();

        message.setChatId(String.valueOf(user.getChatId()));
        message.setText(text);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("message sending error user-{}: {}", user, e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendMessage(User user, SendMessage.SendMessageBuilder messageBuilder) {
        SendMessage message = messageBuilder.chatId(String.valueOf(user.getChatId())).build();
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("message sending error user-{}: {}", user.getChatId(), e.getMessage());
            e.printStackTrace();
        }
    }
}
