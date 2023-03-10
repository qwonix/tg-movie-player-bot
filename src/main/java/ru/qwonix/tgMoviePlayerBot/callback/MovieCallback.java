package ru.qwonix.tgMoviePlayerBot.callback;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import ru.qwonix.tgMoviePlayerBot.bot.MessagesIds;
import ru.qwonix.tgMoviePlayerBot.database.BasicConnectionPool;
import ru.qwonix.tgMoviePlayerBot.database.service.movie.MovieService;
import ru.qwonix.tgMoviePlayerBot.database.service.movie.MovieServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonService;
import ru.qwonix.tgMoviePlayerBot.database.service.season.SeasonServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesService;
import ru.qwonix.tgMoviePlayerBot.database.service.series.SeriesServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserService;
import ru.qwonix.tgMoviePlayerBot.database.service.user.UserServiceImpl;
import ru.qwonix.tgMoviePlayerBot.database.service.video.VideoService;
import ru.qwonix.tgMoviePlayerBot.database.service.video.VideoServiceImpl;
import ru.qwonix.tgMoviePlayerBot.entity.Movie;
import ru.qwonix.tgMoviePlayerBot.entity.User;
import ru.qwonix.tgMoviePlayerBot.entity.Video;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchMovieException;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchVideoException;

import java.util.Optional;

@Slf4j
public class MovieCallback extends Callback {
    private final int movieId;

    private final MovieService movieService = new MovieServiceImpl(BasicConnectionPool.getInstance());
    private final VideoService videoService = new VideoServiceImpl(BasicConnectionPool.getInstance());


    public MovieCallback(User user, int movieId, String callbackId) {
        super(user, callbackId);
        this.movieId = movieId;
    }

    public static JSONObject toJson(int movieId) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.MOVIE);
        jsonData.put("id", movieId);

        return toCallback(jsonData);
    }

    @Override
    public void handle() throws NoSuchMovieException, NoSuchVideoException {
        Optional<Movie> optionalMovie = movieService.find(movieId);
        Movie movie;
        if (optionalMovie.isPresent()) {
            movie = optionalMovie.get();
        } else {
            throw new NoSuchMovieException("Такого фильма не существует. Попробуйте найти его заново.");
        }

        Optional<Video> maxPriorityOptionalVideo = videoService.findMaxPriorityByMovie(movie);

        Video maxPriorityVideo;
        if (maxPriorityOptionalVideo.isPresent()) {
            maxPriorityVideo = maxPriorityOptionalVideo.get();
        } else {
            throw new NoSuchVideoException("Видео не найдено. Попробуйте заново.");
        }

        String movieText = createText(movie);

        MessagesIds messagesIds = user.getMessagesIds();
        if (messagesIds.hasSeasonMessageId()) {
            botUtils.deleteMessage(user, messagesIds.getSeasonMessageId());
            messagesIds.setSeasonMessageId(null);
        }
        if (messagesIds.hasSeriesMessageId()) {
            botUtils.deleteMessage(user, messagesIds.getSeriesMessageId());
            messagesIds.setSeriesMessageId(null);
        }

        if (messagesIds.hasEpisodeMessageId()) {
            botUtils.editPhotoWithMarkdownText(user
                    , messagesIds.getEpisodeMessageId()
                    , movieText
                    , movie.getPreviewTgFileId());
        } else {
            Integer movieMessageId = botUtils.sendPhotoWithMarkdownText(user
                    , movieText
                    , movie.getPreviewTgFileId());
            messagesIds.setEpisodeMessageId(movieMessageId);
        }

        new VideoCallback(user, maxPriorityVideo.getId(), null).handle();

        userService.merge(user);
    }

    private static String createText(Movie movie) {
        return String.format("*%s*\n", movie.getTitle())
                + '\n'
                + String.format("_%s_", movie.getDescription());
    }
}