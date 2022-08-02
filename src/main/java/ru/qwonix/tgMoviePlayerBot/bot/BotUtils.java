package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.qwonix.tgMoviePlayerBot.database.dao.DaoContext;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class BotUtils {
    private final Bot bot;
    private final DaoContext daoContext;

    public BotUtils(BotContext botContext) {
        this.bot = botContext.getBot();
        this.daoContext = botContext.getDaoContext();
    }

    public static InlineKeyboardMarkup createOneRowLinkKeyboard(Map<String, String> buttons) {
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

    public static List<List<InlineKeyboardButton>> createOneRowCallbackKeyboard(Map<String, String> buttons) {
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
        return rowsInline;
    }

    public static InlineKeyboardMarkup createTwoRowsCallbackKeyboard(Map<String, String> buttons) {
        if (buttons.size() < 6) {
            return new InlineKeyboardMarkup(createOneRowCallbackKeyboard(buttons));
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        Iterator<Map.Entry<String, String>> iterator = buttons.entrySet().iterator();
        while (iterator.hasNext()) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            Map.Entry<String, String> pair = iterator.next();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(pair.getKey());
            inlineKeyboardButton.setCallbackData(pair.getValue());

            rowInline.add(inlineKeyboardButton);

            if (iterator.hasNext()) {
                pair = iterator.next();
                inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(pair.getKey());
                inlineKeyboardButton.setCallbackData(pair.getValue());

                rowInline.add(inlineKeyboardButton);
            }
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private static String escapeMarkdownMessage(String markdownMessage) {
        return markdownMessage
                .replace("-", "\\-")
                .replace("!", "\\!")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace(".", "\\.");
    }

    public Integer sendVideo(User user, String fileId) {
        return this.sendVideo(user
                , SendVideo.builder()
                        .disableNotification(true)
                        .video(new InputFile(fileId)));
    }

    public Integer sendText(User user, String text) {
        return this.sendMessage(user
                , SendMessage.builder()
                        .chatId(String.valueOf(user.getChatId()))
                        .text(text));
    }

    public Integer sendMarkdownTextWithPhoto(User user, String markdownMessage, String photoFileId) {
        return this.sendPhoto(user
                , SendPhoto.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .photo(new InputFile(photoFileId)));
    }

    public Integer sendMarkdownText(User user, String markdownMessage) {
        return this.sendMessage(user
                , SendMessage.builder()
                        .text(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2"));
    }

    public Integer sendMarkdownTextWithKeyBoardAndPhoto(User user, String markdownMessage, InlineKeyboardMarkup keyboard, String photoFileId) {
        return this.sendPhoto(user
                , SendPhoto.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .photo(new InputFile(photoFileId))
                        .replyMarkup(keyboard));
    }

    public Integer sendMarkdownTextWithKeyBoard(User user, String markdownMessage, InlineKeyboardMarkup keyboard) {
        return this.sendMessage(user
                , SendMessage.builder()
                        .text(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .replyMarkup(keyboard));
    }

    private Integer sendPhoto(User user, SendPhoto.SendPhotoBuilder photoBuilder) {
        SendPhoto photo = photoBuilder.chatId(String.valueOf(user.getChatId())).build();
        try {
            Message execute = bot.execute(photo);
            return execute.getMessageId();
        } catch (TelegramApiException e) {
            log.error("photo sending error " + user, e);
        }
        return null;
    }

    private Integer sendVideo(User user, SendVideo.SendVideoBuilder videoBuilder) {
        SendVideo photo = videoBuilder.chatId(String.valueOf(user.getChatId())).build();
        try {
            Message execute = bot.execute(photo);
            return execute.getMessageId();
        } catch (TelegramApiException e) {
            log.error("video sending error " + user, e);
        }
        return null;
    }

    private Integer sendMessage(User user, SendMessage.SendMessageBuilder messageBuilder) {
        SendMessage message = messageBuilder.chatId(String.valueOf(user.getChatId())).build();
        try {
            Message execute = bot.execute(message);
            return execute.getMessageId();
        } catch (TelegramApiException e) {
            log.error("message sending error " + user, e);
        }
        return null;
    }

    public void deleteMessage(User user, Integer messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(String.valueOf(user.getChatId()))
                .messageId(messageId).build();
        try {
            bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("message deleting error " + user, e);
        }
    }
}
