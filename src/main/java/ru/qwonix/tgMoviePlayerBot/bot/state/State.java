package ru.qwonix.tgMoviePlayerBot.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import ru.qwonix.tgMoviePlayerBot.bot.Bot;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.callback.*;
import ru.qwonix.tgMoviePlayerBot.database.BasicConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserService;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserServiceImpl;
import ru.qwonix.tgMoviePlayerBot.entity.User;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchCallbackException;

import java.util.Comparator;
import java.util.List;

@Slf4j
public abstract class State {
    public enum StateType {
        DEFAULT, SEARCH
    }

    protected final Update update;
    protected final User user;

    protected final BotUtils botUtils = new BotUtils(Bot.getInstance());
    protected final UserService userService = new UserServiceImpl(BasicConnectionPool.getInstance());

    public State(User user, Update update) {
        this.update = update;
        this.user = user;
    }

    public void onVideo() {
        if (!user.isAdmin()) {
            botUtils.sendMarkdownText(user
                    , "Вы не являетесь администратором. Для получения прав используйте /admin <password>");
            return;
        }
        Video video = update.getMessage().getVideo();

        log.info("user {} send video {}", user, video);

        if (user.isAdmin()) {
            botUtils.sendMarkdownTextWithReplay(user
                    , "getFileId: `" + video.getFileId() + "`" + '\n' + "duration (sec): `" + video.getDuration() + "`"
                    , update.getMessage().getMessageId());
        } else {
            botUtils.sendMarkdownText(user
                    , "Вы не являетесь администратором. Для получения прав используйте /admin <password>");

        }
    }

    public void onPhoto() {
        if (!user.isAdmin()) {
            botUtils.sendMarkdownText(user
                    , "Вы не являетесь администратором. Для получения прав используйте /admin <password>");
            return;
        }

        List<PhotoSize> photos = update.getMessage().getPhoto();
        log.info("user {} send {} photos", user, photos.size());
        PhotoSize photoSize = photos.stream().max(Comparator.comparingInt(PhotoSize::getFileSize)).get();

        botUtils.sendMarkdownTextWithReplay(user
                , "`" + photoSize.getFileId() + "`"
                , update.getMessage().getMessageId());

    }

    public void onCallback() {
        String callbackData = update.getCallbackQuery().getData();
        String callbackId = update.getCallbackQuery().getId();

        JSONObject data = new JSONObject(callbackData).getJSONObject("data");
        String dataTypeString = data.getString("dataType");

        Callback.DataType dataType = Callback.DataType.valueOf(dataTypeString);

        Callback callback;
        switch (dataType) {
            case VIDEO:
                int videoId = data.getInt("id");
                callback = new VideoCallback(user, videoId, callbackId);
                break;
            case EPISODE:
                int episodeId = data.getInt("id");
                callback = new EpisodeCallback(user, episodeId, callbackId);
                break;
            case SEASON:
                int seasonId = data.getInt("id");
                int seasonPage = data.getInt("page");
                callback = new SeasonCallback(user, seasonId, seasonPage, callbackId);
                break;
            case SERIES:
                int seriesId = data.getInt("id");
                int seriesPage = data.getInt("page");
                callback = new SeriesCallback(user, seriesId, seriesPage, callbackId);
                break;
            case MOVIE:
                int movieId = data.getInt("id");
                callback = new MovieCallback(user, movieId, callbackId);
                break;
            case EMPTY:
            default:
                callback = new EmptyCallback(user, update, callbackId);
        }

        log.info("user {} callback {}", user, callback);
        try {
            callback.handle();
            callback.confirm();
        } catch (NoSuchCallbackException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void onText();
}
