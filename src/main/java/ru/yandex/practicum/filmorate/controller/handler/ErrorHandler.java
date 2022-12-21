package ru.yandex.practicum.filmorate.controller.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.ErrorResponse;
import ru.yandex.practicum.filmorate.model.ValidationErrorResponse;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(IncorrectParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidateUser(final RuntimeException exp) {
        return new ErrorResponse(exp.getMessage());
    }

    @ExceptionHandler({FilmNotFoundException.class, UserNotFoundException.class,
            MpaNotFoundException.class, GenreNotFoundException.class, ReviewNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFilmNotFound(final RuntimeException exp) {
        return new ErrorResponse(exp.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.ALREADY_REPORTED)
    public ErrorResponse handleDuplicateLike(final DuplicateLikeException exp) {
        return new ErrorResponse(exp.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleErrorCountResult(final CountOfResultNotExpectedException exp) {
        return new ErrorResponse(exp.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleBindException(MethodArgumentNotValidException exp) {
        //Ошибок валидации может быть несколько - возвращаем информацию по всем полям
        Map<String, String> errors = exp.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        Objects.requireNonNull(DefaultMessageSourceResolvable::getDefaultMessage)));
        log.error(errors.toString());
        return new ValidationErrorResponse(errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable exp) {
        log.error("Произошла непредвиденная ошибка.{}", exp.getMessage(), exp);
        return new ErrorResponse("Произошла непредвиденная ошибка.");
    }

}
