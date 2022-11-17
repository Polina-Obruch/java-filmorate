package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService service;

    @GetMapping
    public List<Film> getFilms() {
        log.debug("Выданы все фильмы");
        return service.getFilms();
    }

    @PostMapping
    public Film addFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        validateFilm(bindingResult);
        Film saveFilm = service.addFilm(film);
        log.debug("Новый фильм добавлен. Выданный id = " + saveFilm.getId());
        return saveFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        validateFilm(bindingResult);
        Film saveFilm = service.updateFilm(film);
        log.debug("Фильм с id = " + film.getId() + " был обновлен");
        return saveFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike() {

    }


    //Для подробной записи ошибок в лог
    private void validateFilm(BindingResult bindingResult) {
        String errorMessage;

        if (bindingResult.hasFieldErrors("name")) {
            errorMessage = "Ошибка валидации фильма. Название фильма не может быть пустым.";
            log.error(errorMessage);
            throw new FilmValidationException(errorMessage);
        }

        if (bindingResult.hasFieldErrors("description")) {
            errorMessage = "Ошибка валидации фильма. Описание не может превышать 200 символов.";
            log.error(errorMessage);
            throw new FilmValidationException(errorMessage);
        }

        if (bindingResult.hasFieldErrors("releaseDate")) {
            errorMessage = "Ошибка валидации фильма. Дата релиза не может быть раньше 28 декабря 1895 года.";
            log.error(errorMessage);
            throw new FilmValidationException(errorMessage);
        }

        if (bindingResult.hasFieldErrors("duration")) {
            errorMessage = "Ошибка валидации фильма. Продолжительность фильма не может быть отрицательным.";
            log.error(errorMessage);
            throw new FilmValidationException(errorMessage);
        }
    }
}
