package ru.qwonix.tgMoviePlayerBot.bot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeService;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


@Slf4j
public class EpisodeCallback extends Callback {
    private final BotContext botContext;
    private final ChatContext chatContext;

    public EpisodeCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }

    public static JSONObject toJSON(int episodeId) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.EPISODE);
        jsonData.put("id", episodeId);

        return Callback.toCallbackJson(jsonData);
    }

    public void handleCallback(int episodeId) {
        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();
        Optional<Episode> optionalEpisode = episodeService.find(episodeId);

        if (optionalEpisode.isPresent()) {
            Episode episode = optionalEpisode.get();

            String text = String.format("`%s сезон %s серия` – *%s*\n", episode.getSeason().getNumber(), episode.getNumber(), episode.getTitle())
                    + '\n'
                    + String.format("_%s_\n", episode.getDescription())
                    + '\n'
                    + String.format("Дата выхода: %s года\n", episode.getReleaseDate().format(
                    DateTimeFormatter.ofPattern("d MMMM y", Locale.forLanguageTag("ru"))))
                    + String.format("Страна: *%s* (_%s_)", episode.getCountry(), episode.getLanguage());


            BotUtils botUtils = new BotUtils(botContext);
            MessagesIds messagesIds = chatContext.getUser().getMessagesIds();
            if (messagesIds.hasEpisodeMessageId()) {
                botUtils.editMarkdownTextWithPhoto(chatContext.getUser()
                        , messagesIds.getEpisodeMessageId()
                        , text
                        , episode.getPreviewFileId());

                botUtils.editVideoWithKeyboard(chatContext.getUser()
                        , messagesIds.getVideoMessageId()
                        , episode.getVideoFileId()
                        , new InlineKeyboardMarkup(createControlButtons(episode)));

            } else {
                Integer episodeMessageId = botUtils.sendMarkdownTextWithPhoto(chatContext.getUser()
                        , text
                        , episode.getPreviewFileId());
                Integer videoMessageId = botUtils.sendVideoWithKeyboard(chatContext.getUser()
                        , episode.getVideoFileId()
                        , new InlineKeyboardMarkup(createControlButtons(episode)));

                messagesIds.setEpisodeMessageId(episodeMessageId);
                messagesIds.setVideoMessageId(videoMessageId);
            }
            botContext.getDatabaseContext().getUserService().merge(chatContext.getUser());
        } else {
            String text = "Такого видео не существует. `Попробуйте найти его заново.`";
            log.error("no video with {} id", episodeId);
            new BotUtils(botContext).sendMarkdownText(chatContext.getUser(), text);
        }
    }

    @Override
    public void handleCallback(JSONObject callbackData) {
        int episodeId = callbackData.getInt("id");
        handleCallback(episodeId);
    }

    private List<List<InlineKeyboardButton>> createControlButtons(Episode currentEpisode) {
        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();
        int currentEpisodeId = currentEpisode.getId();

        Optional<Episode> nextEpisode = episodeService.findNext(currentEpisodeId);
        Optional<Episode> previousEpisode = episodeService.findPrevious(currentEpisodeId);

        InlineKeyboardButton previous;
        InlineKeyboardButton next;

        if (nextEpisode.isPresent()) {
            next = InlineKeyboardButton.builder()
                    .callbackData(EpisodeCallback.toJSON(nextEpisode.get().getId()).toString())
                    .text("›").build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData(EpisodeCallback.toJSON(currentEpisodeId).toString())
                    .text("×").build();
        }

        if (previousEpisode.isPresent()) {
            previous = InlineKeyboardButton.builder()
                    .callbackData(EpisodeCallback.toJSON(previousEpisode.get().getId()).toString())
                    .text("‹").build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(EpisodeCallback.toJSON(currentEpisodeId).toString())
                    .text("×").build();
        }

        int seasonEpisodesCount = episodeService.countAllBySeason(currentEpisode.getSeason());
        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData(EpisodeCallback.toJSON(currentEpisodeId).toString())
                .text(currentEpisode.getNumber() + "/" + seasonEpisodesCount).build();

        return Arrays.asList(Arrays.asList(previous, current, next));
    }
}
