package ru.qwonix.tgMoviePlayerBot.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.callback.*;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchCallbackException;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class State {
    protected final ChatContext chatContext;
    protected final BotContext botContext;

    public State(ChatContext chatContext, BotContext botContext) {
        this.chatContext = chatContext;
        this.botContext = botContext;
    }

    public static State getState(StateType stateType, ChatContext chatContext, BotContext botContext) {
        switch (stateType) {
            case SEARCH:
                return new SearchState(chatContext, botContext);
            case DEFAULT:
            default:
                return new DefaultState(chatContext, botContext);
        }
    }

    public abstract void onText();

    public void onVideo() {
        BotUtils botUtils = new BotUtils(botContext);

        if (!chatContext.getUser().isAdmin()) {
            botUtils.sendMarkdownText(chatContext.getUser()
                    , "Вы не являетесь администратором. Для получения прав используйте /admin <password>");
            return;
        }
        Update update = chatContext.getUpdate();
        Video video = update.getMessage().getVideo();

        log.info("user {} send video {}", chatContext.getUser(), video);

        if (update.getMessage().getCaption() != null) {
            String text = update.getMessage().getCaption();

            try {
                int episodeId = Integer.parseInt(text);
                Integer duration = video.getDuration();
                String fileId = video.getFileId();
                Optional<Episode> episodeOptional = botContext.getDatabaseContext().getEpisodeService().find(episodeId);
                if (episodeOptional.isPresent()) {
                    Episode episode = episodeOptional.get();
                    episode.setDuration(Duration.ofSeconds(duration));
                    episode.setVideoFileId(fileId);
                    botContext.getDatabaseContext().getEpisodeService().insertOrUpdate(episode);

                    log.info("video {} file id and duration has been updated", episode);
                    return;
                }

            } catch (NumberFormatException e) {
                log.info("can not parse episode id");
            }
        }

        if (chatContext.getUser().isAdmin()) {
            botUtils.sendMarkdownTextWithReplay(chatContext.getUser()
                    , "getFileId: `" + video.getFileId() + "`" + '\n' + "duration (sec): `" + video.getDuration() + "`"
                    , update.getMessage().getMessageId());
        } else {
            botUtils.sendMarkdownText(chatContext.getUser()
                    , "Вы не являетесь администратором. Для получения прав используйте /admin <password>");

        }
    }

    public void onPhoto() {
        BotUtils botUtils = new BotUtils(botContext);
        if (!chatContext.getUser().isAdmin()) {
            botUtils.sendMarkdownText(chatContext.getUser()
                    , "Вы не являетесь администратором. Для получения прав используйте /admin <password>");
            return;
        }
        Update update = chatContext.getUpdate();

        List<PhotoSize> photos = update.getMessage().getPhoto();
        log.info("user {} send {} photos", chatContext.getUser(), photos.size());
        PhotoSize photoSize = photos.stream().max(Comparator.comparingInt(PhotoSize::getFileSize)).get();

        if (update.getMessage().getCaption() != null) {
            String text = update.getMessage().getCaption();

            try {
                int episodeId = Integer.parseInt(text);

                String fileId = photoSize.getFileId();
                Optional<Episode> episodeOptional = botContext.getDatabaseContext().getEpisodeService().find(episodeId);
                if (episodeOptional.isPresent()) {
                    Episode episode = episodeOptional.get();
                    episode.setPreviewFileId(fileId);
                    botContext.getDatabaseContext().getEpisodeService().insertOrUpdate(episode);

                    log.info("video {} preview file id has been updated", episode);
                    return;
                }

            } catch (NumberFormatException e) {
                log.info("can not parse episode id");
            }
        }

        botUtils.sendMarkdownTextWithReplay(chatContext.getUser()
                , "`" + photoSize.getFileId() + "`"
                , update.getMessage().getMessageId());

    }

    public void onCallback() {
        CallbackQuery callbackQuery = chatContext.getUpdate().getCallbackQuery();

        String data = callbackQuery.getData();

        if (data.equals("NaN")) {
            log.info("user {} send empty callback", chatContext.getUser());
            new BotUtils(botContext).confirmCallback(callbackQuery.getId());
            return;
        }

        log.info("user {} callback {}", chatContext.getUser(), data);

        JSONObject jsonObject = Callback.parseCallback(data);
        DataType dataType = DataType.valueOf(jsonObject.getString("dataType"));

        Callback callback;
        switch (dataType) {
            case EPISODE:
                callback = new EpisodeCallback(jsonObject);
                break;

            case SEASON:
                callback = new SeasonCallback(jsonObject);
                break;

            case SERIES:
                callback = new SeriesCallback(jsonObject);
                break;

            case QUERY:
//                callback = new QueryCallback(jsonObject);

            default:
                log.info("no such case for {}", dataType);
                return;
        }
        try {
            callback.handleCallback(botContext, chatContext);
        } catch (NoSuchCallbackException e) {
            new BotUtils(botContext).executeAlertWithText(chatContext.getUpdate().getCallbackQuery().getId()
                    , e.getMessage()
                    , false);
        }
    }


    public enum StateType {
        DEFAULT, SEARCH
    }
}
