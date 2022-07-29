package ru.qwonix.tgMoviePlayerBot.bot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.database.servie.SeriesService;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;


@Slf4j
public class EpisodeCallback extends Callback {
    private final BotContext botContext;
    private final ChatContext chatContext;

    public EpisodeCallback(BotContext botContext, ChatContext chatContext) {
        this.botContext = botContext;
        this.chatContext = chatContext;
    }

    public static JSONObject toJSON(int episodeId) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.EPISODE);
        jsonData.put("id", episodeId);

        JSONObject jsonCallback = new JSONObject();
        jsonCallback.put("action", Action.SELECT);
        jsonCallback.put("data", jsonData);

        return jsonCallback;
    }

    @Override
    public void handleCallback(JSONObject callbackData) {
        int episodeId = callbackData.getInt("id");

        SeriesService seriesService = botContext.getDaoContext().getSeriesService();
        Optional<Episode> optionalEpisode = seriesService.findEpisode(episodeId);

        if (optionalEpisode.isPresent()) {
            Episode episode = optionalEpisode.get();

            String text = String.format("`%s сезон %s серия` – *%s*\n", episode.getSeason().getNumber(), episode.getNumber(), episode.getName())
                    + '\n'
                    + String.format("_%s_\n", episode.getDescription())
                    + '\n'
                    + String.format("Дата выхода: %s года\n", episode.getReleaseDate().format(
                    DateTimeFormatter.ofPattern("d MMMM y", Locale.forLanguageTag("ru"))))
                    + String.format("Страна: *%s* (_%s_)", episode.getCountry(), episode.getLanguage());

            BotUtils botUtils = new BotUtils(botContext);
            botUtils.sendMarkdownTextWithPhoto(chatContext.getUser()
                    , text
                    , episode.getPreviewFileId());
            botUtils.sendVideo(chatContext.getUser(), episode.getVideoFileId());
        } else {
            String text = "Такого видео не существует. `Попробуйте найти его заново.`";
            log.error("no video with {} id", episodeId);
            new BotUtils(botContext).sendMarkdownText(chatContext.getUser(), text);
        }
    }
}
