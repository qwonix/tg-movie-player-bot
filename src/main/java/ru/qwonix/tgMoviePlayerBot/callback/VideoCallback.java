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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
public class VideoCallback extends Callback {
    private final int videoId;

    public VideoCallback(int videoId) {
        this.videoId = videoId;
    }

    public VideoCallback(Video video) {
        this.videoId = video.getId();
    }

    public VideoCallback(JSONObject callbackData) {
        this.videoId = callbackData.getInt("id");
    }

    @Override
    public JSONObject toCallback() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.VIDEO);
        jsonData.put("id", videoId);

        return toCallback(jsonData);
    }

    @Override
    public void handleCallback(BotContext botContext, ChatContext chatContext) throws NoSuchVideoException, NoSuchEpisodeException {
        BotUtils botUtils = new BotUtils(botContext);
        EpisodeService episodeService = botContext.getDatabaseContext().getEpisodeService();
        Optional<Video> optionalVideo = botContext.getDatabaseContext().getVideoService().find(videoId);
        Video video;
        if (optionalVideo.isPresent()) {
            video = optionalVideo.get();
        } else {
            throw new NoSuchVideoException("Такого видео не существует. Попробуйте найти его заново.");
        }

        Optional<Episode> optionalEpisode = episodeService.findByVideo(video);
        Episode episode;
        if (optionalEpisode.isPresent()) {
            episode = optionalEpisode.get();
        } else {
            throw new NoSuchEpisodeException("Нету эпизода с таким видео");
        }

        Optional<Episode> nextEpisode = episodeService.findNext(episode);
        Optional<Episode> previousEpisode = episodeService.findPrevious(episode);
        int seasonEpisodesCount = episodeService.countAllBySeason(episode.getSeason());

        List<List<InlineKeyboardButton>> controlButtons
                = EpisodeCallback.createControlButtons(episode, nextEpisode, previousEpisode, seasonEpisodesCount);

        List<Video> episodeVideos = episode.getVideos();
        episodeVideos.remove(video);

        List<List<InlineKeyboardButton>> videoVersions = VideoCallback.createVideosButtons(episodeVideos);
        controlButtons.addAll(videoVersions);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(controlButtons);

        MessagesIds messagesIds = chatContext.getUser().getMessagesIds();
        if (messagesIds.hasVideoMessageId()) {
            botUtils.editVideoWithMarkdownTextKeyboard(chatContext.getUser()
                    , messagesIds.getVideoMessageId()
                    , BotUtils.PROVIDED_BY_TEXT
                    , video.getVideoFileId()
                    , keyboard);
        } else {
            Integer videoMessageId = botUtils.sendVideoWithMarkdownTextKeyboard(chatContext.getUser()
                    , BotUtils.PROVIDED_BY_TEXT
                    , video.getVideoFileId()
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
