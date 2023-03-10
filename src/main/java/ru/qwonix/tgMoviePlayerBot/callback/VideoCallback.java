package ru.qwonix.tgMoviePlayerBot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.database.BasicConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.service.video.VideoService;
import ru.qwonix.tgMoviePlayerBot.database.service.video.VideoServiceImpl;
import ru.qwonix.tgMoviePlayerBot.entity.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.entity.User;
import ru.qwonix.tgMoviePlayerBot.entity.Video;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchVideoException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
public class VideoCallback extends Callback {
    private final int videoId;

    private final VideoService videoService = new VideoServiceImpl(BasicConnectionPool.getInstance());

    public VideoCallback(User user, int videoId, String callbackId) {
        super(user, callbackId);
        this.videoId = videoId;
    }

    public static JSONObject toJson(int videoId) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.VIDEO);
        jsonData.put("id", videoId);

        return toCallback(jsonData);
    }

    @Override
    public void handle() throws NoSuchVideoException {
        Optional<Video> optionalVideo = videoService.find(videoId);
        Video video;
        if (optionalVideo.isPresent()) {
            video = optionalVideo.get();
        } else {
            throw new NoSuchVideoException("Такого видео не существует. Попробуйте найти его заново.");
        }

        List<Video> videos = videoService.findAllVideoByVideo(video);
        videos.remove(video);

        List<List<InlineKeyboardButton>> buttons = VideoCallback.createVideosButtons(videos);

        InlineKeyboardMarkup keyboard = null;
        if (!buttons.isEmpty()) {
            keyboard = new InlineKeyboardMarkup(buttons);
        }

        MessagesIds messagesIds = user.getMessagesIds();
        if (messagesIds.hasVideoMessageId()) {
            botUtils.editVideoWithMarkdownTextAndKeyboard(user
                    , messagesIds.getVideoMessageId()
                    , BotUtils.PROVIDED_BY_TEXT
                    , video.getVideoTgFileId()
                    , keyboard);
        } else {
            Integer videoMessageId = botUtils.sendVideoWithMarkdownTextAndKeyboard(user
                    , BotUtils.PROVIDED_BY_TEXT
                    , video.getVideoTgFileId()
                    , keyboard);
            messagesIds.setVideoMessageId(videoMessageId);
        }

        userService.merge(user);
    }


    public static List<List<InlineKeyboardButton>> createVideosButtons(List<Video> episodeVideos) {
        Map<String, String> keyboardMap = new LinkedHashMap<>();
        for (Video video : episodeVideos) {
            JSONObject videoCallback = VideoCallback.toJson(video.getId());
            String text = String.format("%dр subs:%s dub:%s", video.getResolution(), video.getSubtitlesLanguage(), video.getAudioLanguage());

            keyboardMap.put(text, videoCallback.toString());
        }
        return BotUtils.createOneRowCallbackKeyboard(keyboardMap);
    }
}
