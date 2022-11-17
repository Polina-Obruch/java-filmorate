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
        validate(bindingResult);
        Film saveFilm = service.addFilm(film);
        log.debug("Новый фильм добавлен. Выданный id = " + saveFilm.getId());
        return saveFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        validate(bindingResult);
        service.updateFilm(film);
        log.debug("Фильм с id = " + film.getId() + " был обновлен");
        return film;
    }


    //Для подробной записи ошибок в лог
    private void validate(BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("name")) {
            log.debug("Ошибка валидации фильма. Название фильма не может быть пустым");
            throw new FilmValidationException();
        }

        if (bindingResult.hasFieldErrors("description")) {
            log.debug("Ошибка валидации фильма. Описание не может превышать 200 символов");
            throw new FilmValidationException();
        }

        if (bindingResult.hasFieldErrors("releaseDate")) {
            log.debug("Ошибка валидации фильма. Дата релиза не может быть раньше 28 декабря 1895 года");
            throw new FilmValidationException();
        }

        if (bindingResult.hasFieldErrors("duration")) {
            log.debug("Ошибка валидации фильма. Продолжительность фильма не может быть отрицательным");
            throw new FilmValidationException();
        }
    }
}
