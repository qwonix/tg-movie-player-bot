package ru.qwonix.tgMoviePlayerBot.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.qwonix.tgMoviePlayerBot.bot.Bot;
import ru.qwonix.tgMoviePlayerBot.bot.BotCommand;
import ru.qwonix.tgMoviePlayerBot.bot.BotUtils;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
public class DefaultState extends State {

    public DefaultState(User user, Update update) {
        super(user, update);
    }

    @Override
    public void onText() {
        String userMessageText = update.getMessage().getText();
        log.info("user {} send text {}", user, userMessageText);

        String[] allArgs = userMessageText.split(" ");
        String command = allArgs[0].toLowerCase();
        String[] commandArgs = Arrays.copyOfRange(allArgs, 1, allArgs.length);

        try {
            Method commandMethod = BotCommand.getMethodForCommand(command);

            if (commandMethod != null) {
                commandMethod.invoke(new BotCommand(), user, commandArgs);
                return;
            }

            new BotUtils(Bot.getInstance()).sendMarkdownText(user, "Используйте команды и кнопки, бот не имеет интерфейса общения");
        } catch (IllegalAccessException e) {
            log.error("reflective access exception", e);
        } catch (InvocationTargetException e) {
            log.error("called method-command threw an exception", e);
        }
    }
}
