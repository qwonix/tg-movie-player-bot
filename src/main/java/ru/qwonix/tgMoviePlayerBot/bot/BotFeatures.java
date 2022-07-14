package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.qwonix.tgMoviePlayerBot.dao.SeriesService;
import ru.qwonix.tgMoviePlayerBot.user.User;
import ru.qwonix.tgMoviePlayerBot.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class BotFeatures {

    private final Bot bot;
    private final UserService userService;
    private final SeriesService seriesService;

    public BotFeatures(Bot bot, UserService userService, SeriesService seriesService) {
        this.bot = bot;
        this.userService = userService;
        this.seriesService = seriesService;
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

    public static InlineKeyboardMarkup createLinkKeyboard(Map<String, String> buttons) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (Map.Entry<String, String> button : buttons.entrySet()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

            inlineKeyboardButton.setCallbackData("");

            inlineKeyboardButton.setText(button.getKey());
            inlineKeyboardButton.setCallbackData(button.getValue());
            rowInline.add(inlineKeyboardButton);
        }
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public static InlineKeyboardMarkup createCallbackKeyboard(Map<String, String> buttons) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (Map.Entry<String, String> button : buttons.entrySet()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

            inlineKeyboardButton.setCallbackData("");

            inlineKeyboardButton.setText(button.getKey());
            inlineKeyboardButton.setCallbackData(button.getValue());
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
            log.error("message sending error {}: {}", user, e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMarkdownText(User user, String markdownMessage) {
        SendMessage.SendMessageBuilder message = SendMessage.builder()
                .text(markdownMessage)
                .parseMode("MarkdownV2");

        this.sendMessage(user, message);
    }


    public void sendMessage(User user, SendMessage.SendMessageBuilder messageBuilder) {
        SendMessage message = messageBuilder.chatId(String.valueOf(user.getChatId())).build();
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("message sending error {}: {}", user.getChatId(), e.getMessage());
        }
    }
}
