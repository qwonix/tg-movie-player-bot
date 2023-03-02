package ru.qwonix.tgMoviePlayerBot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.database.service.movie.MovieService;
import ru.qwonix.tgMoviePlayerBot.entity.Movie;
import ru.qwonix.tgMoviePlayerBot.entity.Video;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchMovieException;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchVideoException;

import java.util.Optional;

@Slf4j
public class MovieCallback extends Callback {
    private final int movieId;

    public MovieCallback(int movieId) {
        this.movieId = movieId;
    }

    public MovieCallback(Movie movie) {
        this(movie.getId());
    }

    public MovieCallback(JSONObject callbackData) {
        this(callbackData.getInt("id"));
    }

    @Override
    public JSONObject toCallback() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.MOVIE);
        jsonData.put("id", movieId);

        return toCallback(jsonData);
    }

    @Override
    public void handleCallback(BotContext botContext, ChatContext chatContext) throws NoSuchMovieException, NoSuchVideoException {
        BotUtils botUtils = new BotUtils(botContext);

        MovieService movieService = botContext.getDatabaseContext().getMovieService();
        Optional<Movie> optionalMovie = movieService.find(movieId);
        Movie movie;
        if (optionalMovie.isPresent()) {
            movie = optionalMovie.get();
        } else {
            throw new NoSuchMovieException("Такого фильма не существует. Попробуйте найти его заново.");
        }

        Optional<Video> maxPriorityOptionalVideo = botContext.getDatabaseContext().getVideoService()
                .findMaxPriorityByMovie(movie);

        Video maxPriorityVideo;
        if (maxPriorityOptionalVideo.isPresent()) {
            maxPriorityVideo = maxPriorityOptionalVideo.get();
        } else {
            throw new NoSuchVideoException("Видео не найдено. Попробуйте заново.");
        }

        String movieText = createText(movie);

        MessagesIds messagesIds = chatContext.getUser().getMessagesIds();
        if (messagesIds.hasSeasonMessageId()) {
            botUtils.deleteMessage(chatContext.getUser(), messagesIds.getSeasonMessageId());
            messagesIds.setSeasonMessageId(null);
        }
        if (messagesIds.hasSeriesMessageId()) {
            botUtils.deleteMessage(chatContext.getUser(), messagesIds.getSeriesMessageId());
            messagesIds.setSeriesMessageId(null);
        }

        if (messagesIds.hasEpisodeMessageId()) {
            botUtils.editPhotoWithMarkdownText(chatContext.getUser()
                    , messagesIds.getEpisodeMessageId()
                    , movieText
                    , movie.getPreviewTgFileId());
        } else {
            Integer movieMessageId = botUtils.sendPhotoWithMarkdownText(chatContext.getUser()
                    , movieText
                    , movie.getPreviewTgFileId());
            messagesIds.setEpisodeMessageId(movieMessageId);
        }

        new VideoCallback(maxPriorityVideo).handleCallback(botContext, chatContext);

        botContext.getDatabaseContext().getUserService().merge(chatContext.getUser());
        botUtils.confirmCallback(chatContext.getUpdate().getCallbackQuery().getId());
    }

    private static String createText(Movie movie) {
        return String.format("*%s*\n", movie.getTitle())
                + '\n'
                + String.format("_%s_", movie.getDescription());
    }
}