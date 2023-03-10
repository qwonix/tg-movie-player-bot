package ru.qwonix.tgMoviePlayerBot.callback;

import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.qwonix.tgMoviePlayerBot.entity.User;
import ru.qwonix.tgMoviePlayerBot.exception.NoSuchCallbackException;

public class EmptyCallback extends Callback {

    public static JSONObject toJson() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("dataType", DataType.EMPTY);

        return toCallback(jsonData);
    }

    public EmptyCallback(User user, Update update, String callbackId) {
        super(user, callbackId);
    }

    @Override
    public void handle() throws NoSuchCallbackException {

    }
}
