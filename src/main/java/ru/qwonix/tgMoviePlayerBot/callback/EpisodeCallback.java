package ru.qwonix.tgMoviePlayerBot.callback;

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
import ru.qwonix.tgMoviePlayerBot.entity.Video;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchEpisodeException;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchVideoException;

import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
public class EpisodeCallback extends Callback {
    private final int episodeId;

    public EpisodeCallback(JSONObject callbackData) {
        this.episodeId = callbackData.getInt("id");
    }

    public EpisodeCallback(int episodeId) {
        this.episodeId = episodeId;
    }

    public EpisodeCallback(Episode episode) {
        this.episodeId = episode.getId();
    }

    @Override
    public JSONObject toCallback() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.EPISODE);
        jsonData.put("id", episodeId);

        return toCallback(jsonData);
    }


    @Override
    public void handleCallback(BotContext botContext, ChatContext chatContext) throws NoSuchEpisodeException, NoSuchVideoException {
        BotUtils botUtils = new BotUtils(botContext);

        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();
        Optional<Episode> optionalEpisode = episodeService.find(episodeId);
        Episode episode;
        if (optionalEpisode.isPresent()) {
            episode = optionalEpisode.get();
        } else {
            throw new NoSuchEpisodeException("Такого эпизода не существует. Попробуйте найти его заново.");
        }

        Optional<Video> maxPriorityOptionalVideo = botContext.getDatabaseContext().getVideoService()
                .findMaxPriorityByEpisode(episode);

        Video maxPriorityVideo;
        if (maxPriorityOptionalVideo.isPresent()) {
            maxPriorityVideo = maxPriorityOptionalVideo.get();
        } else {
            throw new NoSuchVideoException("no max priority video");
        }

        Optional<Episode> nextEpisode = episodeService.findNext(episode);
        Optional<Episode> previousEpisode = episodeService.findPrevious(episode);
        int seasonEpisodesCount = episodeService.countAllBySeason(episode.getSeason());

        List<List<InlineKeyboardButton>> controlButtons
                = createControlButtons(episode, nextEpisode, previousEpisode, seasonEpisodesCount);

        List<List<InlineKeyboardButton>> videoVersions = VideoCallback.createVideosButtons(episode.getVideos());
        controlButtons.addAll(videoVersions);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(controlButtons);

        String episodeText = createText(episode);

        MessagesIds messagesIds = chatContext.getUser().getMessagesIds();
        if (messagesIds.hasEpisodeMessageId()) {
            botUtils.editMarkdownTextWithPhoto(chatContext.getUser()
                    , messagesIds.getEpisodeMessageId()
                    , episodeText
                    , episode.getPreviewFileId());

            botUtils.editVideoWithMarkdownTextKeyboard(chatContext.getUser()
                    , messagesIds.getVideoMessageId()
                    , BotUtils.PROVIDED_BY_TEXT
                    , maxPriorityVideo.getVideoFileId()
                    , keyboard);

        } else {
            Integer episodeMessageId = botUtils.sendMarkdownTextWithPhoto(chatContext.getUser()
                    , episodeText
                    , episode.getPreviewFileId());
            messagesIds.setEpisodeMessageId(episodeMessageId);

            Integer videoMessageId = botUtils.sendVideoWithMarkdownTextKeyboard(chatContext.getUser()
                    , BotUtils.PROVIDED_BY_TEXT
                    , maxPriorityVideo.getVideoFileId()
                    , keyboard);
            messagesIds.setVideoMessageId(videoMessageId);
        }

        botContext.getDatabaseContext().getUserService().merge(chatContext.getUser());
        botUtils.confirmCallback(chatContext.getUpdate().getCallbackQuery().getId());
    }

    private static String createText(Episode episode) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM y", Locale.forLanguageTag("ru"));

        return String.format("_%s сезон %s серия_ – `%s`\n", episode.getSeason().getNumber(), episode.getNumber(), episode.getTitle())
                + '\n'
                + String.format("_%s_\n", episode.getDescription())
                + '\n'
                + String.format("*Дата выхода:* `%s года`\n", episode.getReleaseDate().format(dateTimeFormatter))
                + String.format("*Страна:* `%s` (_%s_)", episode.getCountry(), episode.getLanguage());
    }


    public static List<List<InlineKeyboardButton>> createControlButtons(
            Episode currentEpisode
            , Optional<Episode> nextEpisode
            , Optional<Episode> previousEpisode
            , int seasonEpisodesCount) {
        InlineKeyboardButton previous;
        InlineKeyboardButton next;

        if (nextEpisode.isPresent()) {
            next = InlineKeyboardButton.builder()
                    .callbackData(new EpisodeCallback(nextEpisode.get()).toCallback().toString())
                    .text("›").build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData("NaN")
                    .text("×").build();
        }

        if (previousEpisode.isPresent()) {
            previous = InlineKeyboardButton.builder()
                    .callbackData(new EpisodeCallback(previousEpisode.get()).toCallback().toString())
                    .text("‹").build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData("NaN")
                    .text("×").build();
        }

        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData("NaN")
                .text(currentEpisode.getNumber() + "/" + seasonEpisodesCount).build();

        return new ArrayList<>(Collections.singletonList(Arrays.asList(previous, current, next)));
    }

}
