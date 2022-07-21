package ru.qwonix.tgMoviePlayerBot.bot.callback;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;

import java.util.Optional;

@AllArgsConstructor
public class SelectCallback extends Callback {
    @SerializedName("dataType")
    private SelectCallbackType selectCallbackType;
    private int id;

    public void action(BotContext botContext, ChatContext chatContext) {
        switch (selectCallbackType) {
            case SERIES:

            case SEASON:
            case EPISODE:
                Optional<Episode> optionalEpisode = botContext.getDaoContext().getSeriesService().findEpisode(id);
                if (optionalEpisode.isPresent()) {
                    Episode episode = optionalEpisode.get();
                    new BotUtils(botContext).sendVideo(chatContext.getUser(), episode.getFileId());
                }
                else {
                    new BotUtils(botContext).sendText(chatContext.getUser(), "Видео с id " + id + "не найдено. Попробуйте найти его заново.");
                }

            default:

        }

    }
}
