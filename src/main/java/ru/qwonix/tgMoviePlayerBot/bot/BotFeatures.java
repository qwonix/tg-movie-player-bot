package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.qwonix.tgMoviePlayerBot.dao.DaoContext;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class BotFeatures {
    private final Bot bot;
    private final DaoContext daoContext;

    public BotFeatures(Bot bot, DaoContext daoContext) {
        this.bot = bot;
        this.daoContext = daoContext;
    }

    public static InlineKeyboardMarkup createLinkKeyboard(Map<String, String> buttons) {
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

    public static InlineKeyboardMarkup createCallbackKeyboard(Map<String, String> buttons) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (Map.Entry<String, String> button : buttons.entrySet()) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

            inlineKeyboardButton.setText(button.getKey());
            inlineKeyboardButton.setCallbackData(button.getValue());
            rowInline.add(inlineKeyboardButton);
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public void sendVideo(User user, String fileId) {
        try {
            SendDocument sendDocument = SendDocument.builder()
                    .document(new InputFile(fileId))
                    .chatId(String.valueOf(user.getChatId()))
                    .build();
            bot.execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendText(User user, String text) {
        SendMessage message = new SendMessage();

        message.setChatId(String.valueOf(user.getChatId()));
        message.setText(text);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("message sending error " + user, e);
            e.printStackTrace();
        }
    }

    public void sendMarkdownText(User user, String markdownMessage) {
        SendMessage.SendMessageBuilder message = SendMessage.builder()
                .text(markdownMessage)
                .parseMode("MarkdownV2");

        this.sendMessage(user, message);
    }

    public void sendMarkdownTextWithKeyBoard(User user, String markdownMessage, InlineKeyboardMarkup keyboard) {
        SendMessage.SendMessageBuilder message = SendMessage.builder()
                .text(markdownMessage)
                .parseMode("MarkdownV2")
                .replyMarkup(keyboard);

        this.sendMessage(user, message);
    }

    public void sendMessage(User user, SendMessage.SendMessageBuilder messageBuilder) {
        SendMessage message = messageBuilder.chatId(String.valueOf(user.getChatId())).build();
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("message sending error " + user, e);
        }
    }
}
