package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.qwonix.tgMoviePlayerBot.entity.Series;
import ru.qwonix.tgMoviePlayerBot.entity.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public enum State {
    DEFAULT {
        @Override
        public void enter(BotContext context) {
            User user = context.getUser();
            Update update = context.getUpdate();
            BotFeatures botFeatures = context.getBotFeatures();

            String userMessageText = update.getMessage().getText();

            String[] allArgs = userMessageText.split(" ");
            String command = allArgs[0].toLowerCase();
            String[] commandArgs = Arrays.copyOfRange(allArgs, 1, allArgs.length);

            try {
                Method commandMethod = commands.get(command);

                if (commandMethod != null) {
                    commandMethod.invoke(context.getBotCommand(), user, commandArgs);
                    return;
                }

                botFeatures.sendText(user, "не понимаю. попробуйте ещё раз!");

            } catch (IllegalAccessException e) {
                log.error("reflective access exception", e);
            } catch (InvocationTargetException e) {
                log.error("called method-command threw an exception", e);
            }
        }

        @Override
        public State nextState() {
            return State.DEFAULT;
        }
    },
    SEARCH {
        @Override
        public void enter(BotContext context) {
            User user = context.getUser();
            Update update = context.getUpdate();
            BotFeatures botFeatures = context.getBotFeatures();

            String searchText = update.getMessage().getText();

            // TODO: 15-Jul-22 smart search for name 

            List<Series> serials = context.getDaoContext().getSeriesService().findAllByNameLike(searchText);

            Map<String, String> keyboard = new HashMap<>();
            StringBuilder sb = new StringBuilder();
            if (serials.isEmpty()) {
                botFeatures.sendMarkdownText(user, "Ничего не найдено :\\(");
                return;
            }

            for (Series series : serials) {
                sb.append(String.format("`%s` *%s*", series.getName(), series.getCountry()));
                sb.append('\n');
                sb.append('\n');
                sb.append(String.format("_%s_", series.getDescription()));
                sb.append('\n');
                sb.append('\n');

                keyboard.put(series.getName(), String.valueOf(series.getId()));
            }


            InlineKeyboardMarkup callbackKeyboard = BotFeatures.createCallbackKeyboard(keyboard);

            botFeatures.sendMarkdownText(user, String.format("Поиск по запросу: `%s`", searchText));

            String escapedMsg = sb.toString().replace("-", "\\-").replace("!", "\\!").replace(".", "\\.");
            botFeatures.sendMarkdownTextWithKeyBoard(user, escapedMsg, callbackKeyboard);
        }

        @Override
        public State nextState() {
            return State.DEFAULT;
        }
    };
    private static final Map<String, Method> commands = new HashMap<>();

    static {
        for (Method m : BotCommand.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {
                Command cmd = m.getAnnotation(Command.class);
                commands.put(cmd.command(), m);
            }
        }
    }

    State() {

    }

    public abstract void enter(BotContext context);

    public abstract State nextState();
}
