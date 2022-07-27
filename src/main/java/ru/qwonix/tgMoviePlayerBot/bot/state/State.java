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
import ru.qwonix.tgMoviePlayerBot.bot.callback.Action;
import ru.qwonix.tgMoviePlayerBot.bot.callback.Callback;
import ru.qwonix.tgMoviePlayerBot.bot.callback.SelectCallback;

import java.util.List;

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
        Update update = chatContext.getUpdate();
        Video video = update.getMessage().getVideo();
        String fileId = video.getFileId();

//        Episode newEpisode = Episode.builder()
//                .number()
//                .name()
//                .description()
//                .releaseDate()
//                .language("Русский")
//                .country("Россия")
//                .duration(Duration.ofSeconds(video.getDuration()))
//                .season()
//                .fileId(video.getFileId())
//                .build();

        log.info("user {} send video {}", chatContext.getUser(), video);
        log.info("video fileId {}", video.getFileId());
        new BotUtils(botContext).sendVideo(chatContext.getUser(), fileId);
    }


    public void onPhoto() {
        Update update = chatContext.getUpdate();
        List<PhotoSize> photos = update.getMessage().getPhoto();

        log.info("user {} send {} photos", chatContext.getUser(), photos.size());
        for (PhotoSize photo : photos) {
            log.info("photo fileId {} getFileUniqueId {} getFilePath {} getFileSize {}"
                    , photo.getFileId(), photo.getFileUniqueId(), photo.getFilePath(), photo.getFileSize());
            new BotUtils(botContext).sendMarkdownTextWithPhoto(chatContext.getUser(), "*как*", photo.getFileId());
        }
    }

    public void onCallback() {
        CallbackQuery callbackQuery = chatContext.getUpdate().getCallbackQuery();
        String callbackData = callbackQuery.getData();

        log.info("user {} callback {}", chatContext.getUser(), callbackData);

        JSONObject jsonObject = new JSONObject(callbackData);
        String actionStr = jsonObject.getString("action");

        JSONObject data = jsonObject.getJSONObject("data");
        Action action = Action.valueOf(actionStr);
        Callback callback = null;
        switch (action) {
            case SELECT:
                callback = new SelectCallback(data);
                break;
            case NEXT_PAGE:
                break;

            case PREVIOUS_PAGE:
                break;

            default:
                log.info("default in switch onCallback");
                return;

        }
        callback.handle(botContext, chatContext);
    }


    public enum StateType {
        DEFAULT, SEARCH
    }
}
