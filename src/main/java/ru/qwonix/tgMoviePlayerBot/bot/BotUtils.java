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
import ru.qwonix.tgMoviePlayerBot.config.TelegramConfig;
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

    public final static String PROVIDED_BY_TEXT = "||*Предоставлено @"
            + TelegramConfig.getProperty(TelegramConfig.BOT_USERNAME).replaceAll("_", "\\\\_")
            + "*||";

    public BotUtils(Bot bot) {
        this.bot = bot;
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

    public void deleteMessageIds(User user, MessagesIds messagesIds) {
        if (messagesIds.hasSeasonMessageId()) {
            deleteMessage(user, messagesIds.getSeasonMessageId());
        }
        if (messagesIds.hasEpisodeMessageId()) {
            deleteMessage(user, messagesIds.getEpisodeMessageId());
        }
        if (messagesIds.hasVideoMessageId()) {
            deleteMessage(user, messagesIds.getVideoMessageId());
        }
        if (messagesIds.hasSeriesMessageId()) {
            deleteMessage(user, messagesIds.getSeriesMessageId());
        }
    }

    public Integer sendVideoWithKeyboard(User user, String fileId, InlineKeyboardMarkup keyboard) {
        return this.sendVideoWithMarkdownTextAndKeyboard(user, null, fileId, keyboard);
    }

    public Integer sendVideoWithMarkdownText(User user, String markdownMessage, String fileId) {
        return this.sendVideoWithMarkdownTextAndKeyboard(user, markdownMessage, fileId, null);
    }

    public Integer sendVideoWithMarkdownTextAndKeyboard(User user, String markdownMessage, String fileId, InlineKeyboardMarkup keyboard) {
        return this.sendVideo(user
                , SendVideo.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .video(new InputFile(fileId))
                        .replyMarkup(keyboard));
    }

    public Integer sendPhotoWithMarkdownText(User user, String markdownMessage, String photoFileId) {
        return this.sendPhotoWithMarkdownTextKeyBoardReply(user, markdownMessage, photoFileId, null, null);
    }

    public Integer sendPhotoWithMarkdownTextAndKeyboard(User user, String markdownMessage, String photoFileId, ReplyKeyboard keyboard) {
        return this.sendPhotoWithMarkdownTextKeyBoardReply(user, markdownMessage, photoFileId, keyboard, null);
    }

    public Integer sendPhotoWithMarkdownTextKeyBoardReply(User user, String markdownMessage, String photoFileId, ReplyKeyboard keyboard, Integer replayMessageId) {
        return this.sendPhoto(user
                , SendPhoto.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .photo(new InputFile(photoFileId))
                        .replyMarkup(keyboard)
                        .replyToMessageId(replayMessageId));
    }

    public Integer sendMarkdownText(User user, String markdownMessage) {
        return this.sendMarkdownTextWithKeyboardAndReplay(user, markdownMessage, null, null);
    }

    public Integer sendMarkdownTextWithKeyBoard(User user, String markdownMessage, ReplyKeyboard keyboard) {
        return this.sendMarkdownTextWithKeyboardAndReplay(user, markdownMessage, keyboard, null);
    }

    public Integer sendMarkdownTextWithReplay(User user, String markdownMessage, Integer replayMessageId) {
        return this.sendMarkdownTextWithKeyboardAndReplay(user, markdownMessage, null, replayMessageId);
    }

    public Integer sendMarkdownTextWithKeyboardAndReplay(User user, String markdownMessage, ReplyKeyboard keyboard, Integer replayMessageId) {
        return this.sendMessage(user
                , SendMessage.builder()
                        .text(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .replyMarkup(keyboard)
                        .replyToMessageId(replayMessageId));
    }

    public void editPhotoWithMarkdownText(User user, Integer messageId, String markdownMessage, String photoFileId) {
        this.editPhotoWithMarkdownTextAndKeyboard(user, messageId, markdownMessage, photoFileId, null);
    }

    public void editPhotoWithKeyboard(User user, int messageId, InlineKeyboardMarkup keyboard, String photoFileId) {
        this.editPhotoWithMarkdownTextAndKeyboard(user, messageId, null, photoFileId, keyboard);
    }

    public void editPhotoWithMarkdownTextAndKeyboard(User user, int messageId, String markdownMessage, String photoFileId, InlineKeyboardMarkup keyboard) {
        this.editMedia(user, messageId
                , EditMessageMedia.builder()
                        .media(new InputMediaPhoto(photoFileId)));

        this.editMessageCaption(user, messageId
                , EditMessageCaption.builder()
                        .caption(escapeMarkdownMessage(markdownMessage))
                        .parseMode("MarkdownV2")
                        .replyMarkup(keyboard));
    }

    public void editVideoWithMarkdownTextAndKeyboard(User user, Integer messageId, String markdownMessage, String fileId, InlineKeyboardMarkup keyboard) {
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


    public void editVideoWithKeyboard(User user, Integer messageId, String fileId, InlineKeyboardMarkup keyboard) {
        this.editMedia(user, messageId
                , EditMessageMedia.builder()
                        .media(new InputMediaVideo(fileId))
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

    public void confirmCallback(String callbackQueryId) {
        this.executeAlertWithText(callbackQueryId, null, null);
    }

    public void executeAlertWithText(String callbackQueryId, String text, Boolean showAlert) {
        AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .showAlert(showAlert)
                .build();

        this.executeAlert(answerCallbackQuery);
    }

    public void executeAlert(String callbackQueryId, Boolean showAlert) {
        AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .showAlert(showAlert)
                .build();

        this.executeAlert(answerCallbackQuery);
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


    public void executeAlert(AnswerCallbackQuery answerCallbackQuery) {
        try {
            bot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("execute alert sending error ", e);
        }
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


    public Boolean deleteMessage(User user, Integer messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(String.valueOf(user.getChatId()))
                .messageId(messageId).build();
        try {
            return bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("message deleting error " + user, e);
        }
        return Boolean.FALSE;
    }
}
