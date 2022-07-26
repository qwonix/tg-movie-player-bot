package ru.qwonix.tgMoviePlayerBot.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import ru.qwonix.tgMoviePlayerBot.bot.BotContext;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.bot.ChatContext;
import ru.qwonix.tgMoviePlayerBot.bot.command.BotCommand;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
public class DefaultState extends State {

    public DefaultState(ChatContext chatContext, BotContext botContext) {
        super(chatContext, botContext);
    }

    @Override
    public void onText() {
        User user = chatContext.getUser();
        Update update = chatContext.getUpdate();

        String userMessageText = update.getMessage().getText();

        String[] allArgs = userMessageText.split(" ");
        String command = allArgs[0].toLowerCase();
        String[] commandArgs = Arrays.copyOfRange(allArgs, 1, allArgs.length);

        try {
            Method commandMethod = BotCommand.getMethodForCommand(command);

            if (commandMethod != null) {
                commandMethod.invoke(new BotCommand(botContext), user, commandArgs);
                return;
            }

            new BotUtils(botContext).sendText(user, "Не понимаю. Попробуйте ещё раз!");
        } catch (IllegalAccessException e) {
            log.error("reflective access exception", e);
        } catch (InvocationTargetException e) {
            log.error("called method-command threw an exception", e);
        }
    }

    @Override
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

}
