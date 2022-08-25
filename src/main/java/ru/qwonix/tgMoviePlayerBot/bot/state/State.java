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
import ru.qwonix.tgMoviePlayerBot.bot.callback.*;

import java.util.Comparator;
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

        log.info("user {} send video {}", chatContext.getUser(), video);
        BotUtils botUtils = new BotUtils(botContext);
        if (chatContext.getUser().isAdmin()) {
            botUtils.sendMarkdownTextWithReplay(chatContext.getUser()
                    , "`" + video.getFileId() + "`" + '\n' + "duration (sec): `" + video.getDuration() + "`"
                    , update.getMessage().getMessageId());
        } else {
            botUtils.sendMarkdownText(chatContext.getUser()
                    , "Вы не являетесь администратором. Для получения прав используйте /admin <password>");

        }
    }


    public void onPhoto() {
        Update update = chatContext.getUpdate();
        BotUtils botUtils = new BotUtils(botContext);

        if (chatContext.getUser().isAdmin()) {
            List<PhotoSize> photos = update.getMessage().getPhoto();
            log.info("user {} send {} photos", chatContext.getUser(), photos.size());

            PhotoSize photoSize = photos.stream().max(Comparator.comparingInt(PhotoSize::getFileSize)).get();
            botUtils.sendMarkdownTextWithReplay(chatContext.getUser()
                    , "`" + photoSize.getFileId() + "`"
                    , update.getMessage().getMessageId());
        } else {
            botUtils.sendMarkdownText(chatContext.getUser()
                    , "Вы не являетесь администратором. Для получения прав используйте /admin <password>");
        }
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

        JSONObject jsonObject = Callback.fromCallbackJson(data);
        DataType dataType = DataType.valueOf(jsonObject.getString("dataType"));

        Callback callback;
        switch (dataType) {
            case EPISODE:
                callback = new EpisodeCallback(botContext, chatContext);
                break;

            case SEASON:
                callback = new SeasonCallback(botContext, chatContext);
                break;

            case SERIES:
                callback = new SeriesCallback(botContext, chatContext);
                break;

            case QUERY:
                callback = new QueryCallback(botContext, chatContext);
                break;

            default:
                log.info("no such case for {}", dataType);
                return;
        }
        callback.handleCallback(jsonObject);
    }


    public enum StateType {
        DEFAULT, SEARCH
    }
}
