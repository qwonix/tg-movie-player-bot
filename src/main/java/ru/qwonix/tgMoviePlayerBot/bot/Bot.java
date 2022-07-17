package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import ru.qwonix.tgMoviePlayerBot.config.BotConfig;
import ru.qwonix.tgMoviePlayerBot.dao.DaoContext;
import ru.qwonix.tgMoviePlayerBot.entity.Episode;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.util.Optional;

@Slf4j
public class Bot extends TelegramLongPollingBot {
    private final BotCommand botCommand;
    private final BotFeatures botFeatures;
    private final DaoContext daoContext;


    public Bot() {
        daoContext = new DaoContext();

        BotFeatures botFeatures = new BotFeatures(this, daoContext);
        this.botFeatures = botFeatures;
        this.botCommand = new BotCommand(botFeatures, daoContext);
    }

    public void onVideo(Update update) {
        Video video = update.getMessage().getVideo();
        String fileId = video.getFileId();

        // FIXME: 14-Jul-22 проверить, содержит ли update информацию об отправлители. заменить доступ, если нет
        User user = User.builder()
                .chatId(update.getMessage().getChatId())
                .name(update.getMessage().getFrom().getFirstName())
                .build();

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

        log.info("user {} send video {}", user, video);
        log.info("video fileid {}", video.getFileId());
        botFeatures.sendVideo(user, fileId);
    }

    public void onCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        User user = User.builder()
                .chatId(callbackQuery.getFrom().getId())
                .name(callbackQuery.getFrom().getFirstName())
                .build();

        log.info("user {} callback {}", user, data);
        String fileId = daoContext.getSeriesService().findEpisode(Integer.parseInt(data))
                .map(Episode::getFileId)
                .orElse("");

        botFeatures.sendVideo(user, fileId);
    }

    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            onCallbackQuery(update);
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            if (update.getMessage().hasVideo()) {
                this.onVideo(update);
            }
            return;
        }

        long chatId = update.getMessage().getChatId();
        User user;
        Optional<User> optionalUser = daoContext.getUserService().findUser(chatId);
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = User.builder()
                    .chatId(chatId)
                    .name(update.getMessage().getFrom().getFirstName())
                    .build();
            daoContext.getUserService().merge(user);
        }

        String userMessageText = update.getMessage().getText();

        BotContext stateContext = new BotContext(user, update, daoContext, botFeatures, botCommand);
        log.debug("user: {}, text: {}", user, userMessageText);

        user.getState().enter(stateContext);
        user.getState().nextState();
    }


    @Override
    public String getBotUsername() {
        return BotConfig.getProperty(BotConfig.BOT_USERNAME);
    }

    @Override
    public String getBotToken() {
        return BotConfig.getProperty(BotConfig.BOT_TOKEN);
    }
}