package ru.qwonix.tgMoviePlayerBot.exception;

public class NoSuchMovieException extends NoSuchCallbackException {
    public NoSuchMovieException(String message) {
        super(message);
    }
}
