package ru.qwonix.tgMoviePlayerBot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.qwonix.tgMoviePlayerBot.bot.Bot;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.database.BasicConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeService;
import ru.qwonix.tgMoviePlayerBot.database.service.episode.EpisodeServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserService;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.video.VideoService;
import ru.qwonix.tgMoviePlayerBot.database.service.video.VideoServiceImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.User;
import ru.qwonix.tgMoviePlayerBot.entity.Video;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchEpisodeException;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchVideoException;

import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
public class EpisodeCallback extends Callback {
    private final int episodeId;

    private final EpisodeService episodeService = new EpisodeServiceImpl(BasicConnectionPool.getInstance());
    private final VideoService videoService = new VideoServiceImpl(BasicConnectionPool.getInstance());

    public EpisodeCallback(User user, int episodeId, String callbackId) {
        super(user, callbackId);
        this.episodeId = episodeId;
    }

    public static JSONObject toJson(int episodeId) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.EPISODE);
        jsonData.put("id", episodeId);

        return toCallback(jsonData);
    }

    @Override
    public void handle() throws NoSuchEpisodeException, NoSuchVideoException {
        Optional<Episode> optionalEpisode = episodeService.find(episodeId);
        Episode episode;
        if (optionalEpisode.isPresent()) {
            episode = optionalEpisode.get();
        } else {
            throw new NoSuchEpisodeException("Такого эпизода не существует. Попробуйте найти его заново.");
        }
        Optional<Video> maxPriorityOptionalVideo = videoService.findMaxPriorityByEpisode(episode);

        Video maxPriorityVideo;
        if (maxPriorityOptionalVideo.isPresent()) {
            maxPriorityVideo = maxPriorityOptionalVideo.get();
        } else {
            throw new NoSuchVideoException("Видео не найдено. Попробуйте заново.");
        }

        Optional<Episode> nextEpisode = episodeService.findNext(episode);
        Optional<Episode> previousEpisode = episodeService.findPrevious(episode);
        int seasonEpisodesCount = episodeService.countAllBySeason(episode.getSeason());

        List<List<InlineKeyboardButton>> controlButtons
                = EpisodeCallback.createControlButtons(episode, nextEpisode, previousEpisode, seasonEpisodesCount);

        String episodeText = createText(episode);
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(controlButtons);

        MessagesIds messagesIds = user.getMessagesIds();
        if (messagesIds.hasEpisodeMessageId()) {
            botUtils.editPhotoWithMarkdownTextAndKeyboard(user
                    , messagesIds.getEpisodeMessageId()
                    , episodeText
                    , episode.getPreviewTgFileId()
                    , keyboard);
        } else {
            Integer episodeMessageId = botUtils.sendPhotoWithMarkdownTextAndKeyboard(user
                    , episodeText
                    , episode.getPreviewTgFileId()
                    , keyboard
            );
            messagesIds.setEpisodeMessageId(episodeMessageId);
        }

        new VideoCallback(user, maxPriorityVideo.getId(), null).handle();

        userService.merge(user);
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
                    .callbackData(EpisodeCallback.toJson(nextEpisode.get().getId()).toString())
                    .text("›").build();
        } else {
            next = InlineKeyboardButton.builder()
                    .callbackData(EmptyCallback.toJson().toString())
                    .text("×").build();
        }

        if (previousEpisode.isPresent()) {
            previous = InlineKeyboardButton.builder()
                    .callbackData(EpisodeCallback.toJson(previousEpisode.get().getId()).toString())
                    .text("‹").build();
        } else {
            previous = InlineKeyboardButton.builder()
                    .callbackData(EmptyCallback.toJson().toString())
                    .text("×").build();
        }

        InlineKeyboardButton current = InlineKeyboardButton.builder()
                .callbackData(EmptyCallback.toJson().toString())
                .text(currentEpisode.getNumber() + "/" + seasonEpisodesCount).build();

        return new ArrayList<>(Collections.singletonList(Arrays.asList(previous, current, next)));
    }

}
