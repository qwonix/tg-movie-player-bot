package ru.qwonix.tgMoviePlayerBot.bot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.config.TelegramConfig;
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeService;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;

import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
public class EpisodeCallback extends Callback {
    private final BotContext botContext;
    private final ChatContext chatContext;

    public EpisodeCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }

    public static JSONObject toCallback(int episodeId) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.EPISODE);
        jsonData.put("id", episodeId);

        return Callback.toCallback(jsonData);
    }

    public void handleCallback(int episodeId) {
        Optional<Episode> optionalEpisode = botContext.getDatabaseContext().getEpisodeService().find(episodeId);

        if (optionalEpisode.isPresent()) {
            this.onEpisodeExists(optionalEpisode.get());

        } else {
            new BotUtils(botContext).executeAlertWithText(chatContext.getUpdate().getCallbackQuery().getId()
                    , "Такого видео не существует. Попробуйте найти его заново."
                    , false);
            log.error("no video with {} id", episodeId);
        }
    }

    private void onEpisodeExists(Episode episode) {
        BotUtils botUtils = new BotUtils(botContext);

        String text = String.format("_%s сезон %s серия_ – `%s`\n", episode.getSeason().getNumber(), episode.getNumber(), episode.getTitle())
                + '\n'
                + String.format("_%s_\n", episode.getDescription())
                + '\n'
                + String.format("*Дата выхода:* `%s года`\n", episode.getReleaseDate().format(
                    DateTimeFormatter.ofPattern("d MMMM y", Locale.forLanguageTag("ru"))))
                + String.format("*Страна:* `%s` (_%s_)", episode.getCountry(), episode.getLanguage());

        MessagesIds messagesIds = chatContext.getUser().getMessagesIds();
        String providedByText = "||*Предоставлено @"
                + TelegramConfig.getProperty(TelegramConfig.BOT_USERNAME).replaceAll("_", "\\\\_")
                + "*||";

        if (messagesIds.hasEpisodeMessageId()) {
            botUtils.editMarkdownTextWithPhoto(chatContext.getUser()
                    , messagesIds.getEpisodeMessageId()
                    , text
                    , episode.getPreviewFileId());

            botUtils.editVideoWithMarkdownTextKeyboard(chatContext.getUser()
                    , messagesIds.getVideoMessageId()
                    , providedByText
                    , episode.getVideoFileId()
                    , new InlineKeyboardMarkup(createControlButtons(episode)));

        } else {
            Integer episodeMessageId = botUtils.sendMarkdownTextWithPhoto(chatContext.getUser()
                    , text
                    , episode.getPreviewFileId());
            messagesIds.setEpisodeMessageId(episodeMessageId);

            Integer videoMessageId = botUtils.sendVideoWithMarkdownTextKeyboard(chatContext.getUser()
                    , providedByText
                    , episode.getVideoFileId()
                    , new InlineKeyboardMarkup(createControlButtons(episode)));
            messagesIds.setVideoMessageId(videoMessageId);
        }

        botContext.getDatabaseContext().getUserService().merge(chatContext.getUser());
        botUtils.confirmCallback(chatContext.getUpdate().getCallbackQuery().getId());
    }

    @Override
    public void handleCallback(JSONObject callbackData) {
        int episodeId = callbackData.getInt("id");
        handleCallback(episodeId);
    }

    private List<List<InlineKeyboardButton>> createControlButtons(Episode currentEpisode) {
        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();

        Optional<Episode> nextEpisode = episodeService.findNext(currentEpisode);
        Optional<Episode> previousEpisode = episodeService.findPrevious(currentEpisode);

        InlineKeyboardButton previous;
        InlineKeyboardButton next;

        if (nextEpisode.isPresent()) {
            next = InlineKeyboardButton.builder()
                    .callbackData(EpisodeCallback.toCallback(nextEpisode.get().getId()).toString())
                    .text("›").build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData("NaN")
                    .text("×").build();
        }

        if (previousEpisode.isPresent()) {
            previous = InlineKeyboardButton.builder()
                    .callbackData(EpisodeCallback.toCallback(previousEpisode.get().getId()).toString())
                    .text("‹").build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData("NaN")
                    .text("×").build();
        }

        int seasonEpisodesCount = episodeService.findLast(currentEpisode.getSeason()).get().getNumber();
        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData("NaN")
                .text(currentEpisode.getNumber() + "/" + seasonEpisodesCount).build();

        return Collections.singletonList(Arrays.asList(previous, current, next));
    }
}
