package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.database.DatabaseContext;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class BotUtils {
    private final Bot bot;
    private final DatabaseContext databaseContext;

    public BotUtils(BotContext botContext) {
        this.bot = botContext.getBot();
        this.databaseContext = botContext.getDatabaseContext();
    }

    public static List<List<InlineKeyboardButton>> createOneRowCallbackKeyboard(Map<String, String> buttons) {
        return BotUtils.convertToCallbackButtons(buttons).map(Arrays::asList).collect(Collectors.toList());
    }

    private static Stream<InlineKeyboardButton> convertToCallbackButtons(Map<String, String> buttons) {
        return buttons.entrySet().stream().map(pair ->
                InlineKeyboardButton.builder()
                        .text(pair.getKey())
                        .callbackData(pair.getValue())
                        .build());
    }

    public static List<List<InlineKeyboardButton>> createTwoRowsCallbackKeyboard(Map<String, String> buttons) {
        if (buttons.size() < BotConfig.getIntProperty(BotConfig.KEYBOARD_COLUMNS_ROW_MAX)) {
            return createOneRowCallbackKeyboard(buttons);
        }
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        int l = buttons.size() / 2;
        List<InlineKeyboardButton> firstPart = convertToCallbackButtons(buttons).limit(l).collect(Collectors.toList());
        List<InlineKeyboardButton> secondPart = convertToCallbackButtons(buttons).skip(l).collect(Collectors.toList());

        for (int i = 0; i < l; i++) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();

            rowInline.add(firstPart.get(i));
            rowInline.add(secondPart.get(i));
            rowsInline.add(rowInline);
        }

        if (buttons.size() % 2 == 1) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(secondPart.get(l));
            rowsInline.add(rowInline);
        }
        return rowsInline;
    }

    private static String escapeMarkdownMessage(String markdownMessage) {
        return markdownMessage
                .replace("-", "\\-")
                .replace("!", "\\!")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("<", "\\<")
                .replace(">", "\\>")
                .replace(".", "\\.");
    }

    public Integer sendVideoWithKeyboard(User user, String fileId, InlineKeyboardMarkup keyboard) {
        return this.sendVideo(user
                , SendVideo.builder()
                        .video(new InputFile(fileId))
                        .replyMarkup(keyboard));
    }

    public Integer sendVideoWithMarkdownText(User user, String markdownMessage, String fileId) {
        return this.sendVideo(user
                , SendVideo.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .video(new InputFile(fileId)));
    }

    public Integer sendVideoWithMarkdownTextKeyboard(User user, String markdownMessage, String fileId, InlineKeyboardMarkup keyboard) {
        return this.sendVideo(user
                , SendVideo.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .video(new InputFile(fileId))
                        .replyMarkup(keyboard));
    }

    public void editVideoWithKeyboard(User user, Integer messageId, String fileId, InlineKeyboardMarkup keyboard) {
        this.editMedia(user, messageId
                , EditMessageMedia.builder()
                        .media(new InputMediaVideo(fileId))
                        .replyMarkup(keyboard));
    }

    public void editVideoWithMarkdownTextKeyboard(User user, Integer messageId, String markdownMessage, String fileId, InlineKeyboardMarkup keyboard) {
        this.editMedia(user, messageId
                , EditMessageMedia.builder()
                        .media(new InputMediaVideo(fileId))
                        .replyMarkup(keyboard));

        this.editMessageCaption(user, messageId
                , EditMessageCaption.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .replyMarkup(keyboard));

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
                        .disableNotification(true)
                        .parseMode("MarkdownV2")
                        .photo(new InputFile(photoFileId)));
    }

    public void editMarkdownTextWithPhoto(User user, Integer messageId, String markdownMessage, String photoFileId) {
        this.editMedia(user, messageId
                , EditMessageMedia.builder()
                        .media(new InputMediaPhoto(photoFileId)));

        this.editMessageCaption(user, messageId
                , EditMessageCaption.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2"));
    }

    public Integer sendMarkdownText(User user, String markdownMessage) {
        return this.sendMessage(user
                , SendMessage.builder()
                        .text(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2"));
    }

    public Integer sendMarkdownTextWithReplay(User user, String markdownMessage, int replayMessageId) {
        return this.sendMessage(user
                , SendMessage.builder()
                        .text(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .replyToMessageId(replayMessageId));
    }

    public Integer sendMarkdownTextWithKeyBoardAndPhoto(User user, String markdownMessage, ReplyKeyboard keyboard, String photoFileId) {
        return this.sendPhoto(user
                , SendPhoto.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .photo(new InputFile(photoFileId))
                        .replyMarkup(keyboard));
    }

    public void editMarkdownTextWithKeyBoardAndPhoto(User user, int messageId, String markdownMessage, InlineKeyboardMarkup keyboard, String photoFileId) {
        this.editMedia(user, messageId
                , EditMessageMedia.builder()
                        .media(new InputMediaPhoto(photoFileId)));

        this.editMessageCaption(user, messageId
                , EditMessageCaption.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .replyMarkup(keyboard));
    }

    public void editKeyBoardAndPhoto(User user, int messageId, InlineKeyboardMarkup keyboard, String photoFileId) {
        this.editMedia(user, messageId
                , EditMessageMedia.builder()
                        .media(new InputMediaPhoto(photoFileId)));

        this.editMessageCaption(user, messageId
                , EditMessageCaption.builder()
                        .parseMode("MarkdownV2")
                        .replyMarkup(keyboard));
    }

    public Integer sendMarkdownTextWithKeyBoard(User user, String markdownMessage, ReplyKeyboard keyboard) {
        return this.sendMessage(user
                , SendMessage.builder()
                        .text(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .replyMarkup(keyboard));
    }

    public void editMarkdownTextWithKeyBoard(User user, int messageId, String markdownMessage, InlineKeyboardMarkup keyboard) {
        this.editMessage(user, messageId
                , EditMessageText.builder()
                        .messageId(messageId)
                        .text(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .replyMarkup(keyboard));
    }

    private Integer sendPhoto(User user, SendPhoto.SendPhotoBuilder photoBuilder) {
        SendPhoto photo = photoBuilder
                .chatId(String.valueOf(user.getChatId()))
                .disableNotification(true)
                .build();
        try {
            Message execute = bot.execute(photo);
            return execute.getMessageId();
        } catch (TelegramApiException e) {
            log.error("photo sending error " + user, e);
        }
        return null;
    }

    private Integer sendVideo(User user, SendVideo.SendVideoBuilder videoBuilder) {
        SendVideo video = videoBuilder
                .chatId(String.valueOf(user.getChatId()))
                .disableNotification(true)
                .build();
        try {
            Message execute = bot.execute(video);
            return execute.getMessageId();
        } catch (TelegramApiException e) {
            log.error("video sending error " + user, e);
        }
        return null;
    }

    private void editMessage(User user, int messageId, EditMessageText.EditMessageTextBuilder editMessageTextBuilder) {
        EditMessageText editMessage = editMessageTextBuilder
                .chatId(String.valueOf(user.getChatId()))
                .messageId(messageId)
                .build();

        try {
            bot.execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("message editing error " + user, e);
        }
    }

    private void editMessageCaption(User user, int messageId, EditMessageCaption.EditMessageCaptionBuilder editMessageCaptionBuilder) {
        EditMessageCaption editMessageCaption = editMessageCaptionBuilder
                .chatId(String.valueOf(user.getChatId()))
                .messageId(messageId)
                .build();

        try {
            bot.execute(editMessageCaption);
        } catch (TelegramApiException e) {
            log.error("message editing error " + user, e);
        }
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

    public void confirmCallback(String callbackQueryId) {
        AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .showAlert(false)
                .build();
        this.executeAlert(answerCallbackQuery);
    }

    public void executeAlert(AnswerCallbackQuery answerCallbackQuery) {
        try {
            bot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("execute alert sending error ", e);
        }
    }

    public void executeAlertWithText(String callbackQueryId, String text, boolean showAlert) {
        AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .showAlert(showAlert)
                .build();

        this.executeAlert(answerCallbackQuery);
    }

    private void editMedia(User user, int messageId, EditMessageMedia.EditMessageMediaBuilder editMessageMediaBuilder) {
        EditMessageMedia editMedia = editMessageMediaBuilder
                .chatId(String.valueOf(user.getChatId()))
                .messageId(messageId)
                .build();

        try {
            bot.execute(editMedia);
        } catch (TelegramApiException e) {
            log.error("media editing eror " + user, e);
        }
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
