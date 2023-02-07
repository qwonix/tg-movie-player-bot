package ru.qwonix.tgMoviePlayerBot.exception;

public class NoSuchVideoException extends NoSuchCallbackException {
    public NoSuchVideoException(String message) {
        super(message);
    }
}
