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

import java.util.*;


@Slf4j
public class VideoCallback extends Callback {
    private final int videoId;

    public VideoCallback(int videoId) {
        this.videoId = videoId;
    }

    public VideoCallback(Video video) {
        this(video.getId());
    }

    public VideoCallback(JSONObject callbackData) {
        this(callbackData.getInt("id"));
    }

    @Override
    public JSONObject toCallback() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.VIDEO);
        jsonData.put("id", videoId);

        return toCallback(jsonData);
    }

    @Override
    public void handleCallback(BotContext botContext, ChatContext chatContext) throws NoSuchVideoException {
        BotUtils botUtils = new BotUtils(botContext);
        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();
        Optional<Video> optionalVideo = botContext.getDatabaseContext().getVideoService().find(videoId);
        Video video;
        if (optionalVideo.isPresent()) {
            video = optionalVideo.get();
        } else {
            throw new NoSuchVideoException("Такого видео не существует. Попробуйте найти его заново.");
        }

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<Video> videos = botContext.getDatabaseContext().getVideoService().findAllVideoByVideo(video);
        videos.remove(video);

        buttons.addAll(VideoCallback.createVideosButtons(videos));

        Optional<Episode> optionalEpisode = episodeService.findByVideo(video);
        if (optionalEpisode.isPresent()) {
            Episode episode = optionalEpisode.get();
            Optional<Episode> nextEpisode = episodeService.findNext(episode);
            Optional<Episode> previousEpisode = episodeService.findPrevious(episode);
            int seasonEpisodesCount = episodeService.countAllBySeason(episode.getSeason());

            List<List<InlineKeyboardButton>> controlButtons
                    = EpisodeCallback.createControlButtons(episode, nextEpisode, previousEpisode, seasonEpisodesCount);
            buttons.addAll(controlButtons);
        }

        InlineKeyboardMarkup keyboard = null;
        if (!buttons.isEmpty()) {
            keyboard = new InlineKeyboardMarkup(buttons);
        }

        MessagesIds messagesIds = chatContext.getUser().getMessagesIds();
        if (messagesIds.hasVideoMessageId()) {
            botUtils.editVideoWithMarkdownTextKeyboard(chatContext.getUser()
                    , messagesIds.getVideoMessageId()
                    , BotUtils.PROVIDED_BY_TEXT
                    , video.getVideoTgFileId()
                    , keyboard);
        } else {
            Integer videoMessageId = botUtils.sendVideoWithMarkdownTextKeyboard(chatContext.getUser()
                    , BotUtils.PROVIDED_BY_TEXT
                    , video.getVideoTgFileId()
                    , keyboard);
            messagesIds.setVideoMessageId(videoMessageId);
        }

        botContext.getDatabaseContext().getUserService().merge(chatContext.getUser());
        botUtils.confirmCallback(chatContext.getUpdate().getCallbackQuery().getId());
    }


    public static List<List<InlineKeyboardButton>> createVideosButtons(List<Video> episodeVideos) {
        Map<String, String> keyboardMap = new LinkedHashMap<>();
        for (Video video : episodeVideos) {
            JSONObject videoCallback = new VideoCallback(video).toCallback();
            String text = String.format("%dр subs:%s dub:%s", video.getResolution(), video.getSubtitlesLanguage(), video.getAudioLanguage());

            keyboardMap.put(text, videoCallback.toString());
        }
        return BotUtils.createOneRowCallbackKeyboard(keyboardMap);
    }
}
