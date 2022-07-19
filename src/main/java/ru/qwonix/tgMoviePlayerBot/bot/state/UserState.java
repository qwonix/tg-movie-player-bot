package ru.qwonix.tgMoviePlayerBot.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotFeatures;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.dao.DaoContext;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.User;

@Slf4j
public abstract class UserState {
    protected final ChatContext chatContext;
    protected final BotContext botContext;

    public UserState(ChatContext chatContext, BotContext botContext) {
        this.chatContext = chatContext;
        this.botContext = botContext;
    }

    public static UserState getState(State state, ChatContext chatContext, BotContext botContext) {
        switch (state) {
            case SEARCH:
                return new SearchState(chatContext, botContext);
            case DEFAULT:
            default:
                return new DefaultState(chatContext, botContext);
        }
    }

    public abstract void onText();

    public abstract void onVideo();

    public void onCallback() {
        CallbackQuery callbackQuery = chatContext.getUpdate().getCallbackQuery();
        String data = callbackQuery.getData();
        User user = chatContext.getUser();
        log.info("user {} callback {}", user, data);

        DaoContext daoContext = botContext.getDaoContext();
        String fileId = daoContext.getSeriesService().findEpisode(Integer.parseInt(data))
                .map(Episode::getFileId)
                .orElse("");

        BotFeatures botFeatures = new BotFeatures(botContext);
        botFeatures.sendVideo(user, fileId);
    }


    public enum State {
        DEFAULT, SEARCH
    }
}
