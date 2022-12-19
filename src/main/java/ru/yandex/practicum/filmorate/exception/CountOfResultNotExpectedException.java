package ru.yandex.practicum.filmorate.exception;

public class CountOfResultNotExpectedException extends RuntimeException {
    public CountOfResultNotExpectedException(String message) {
        super(message);
    }
}
