package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
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
        List<Film> films = service.getFilms();
        log.debug("Список всех фильмов был выдан");
        return films;
    }

    @PostMapping
    public Film addFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        validateFilm(bindingResult);
        Film saveFilm = service.addFilm(film);
        log.debug(String.format("Новый фильм был добавлен. Выданный id =%d", saveFilm.getId()));
        return saveFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        validateFilm(bindingResult);
        Film saveFilm = service.updateFilm(film);
        log.debug(String.format("Фильм с id = %d был обновлен", saveFilm.getId()));
        return saveFilm;
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Integer id) {
        Film saveFilm = service.getFilm(id);
        log.debug(String.format("Фильм с id =%d был выдан", saveFilm.getId()));
        return saveFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        service.addLike(id, userId);
        log.debug(String.format("Фильму с id = %d был поставлен лайк", id));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        service.removeLike(id, userId);
        log.debug(String.format("У фильма с id = %d был удален лайк", id));
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilm(@RequestParam(defaultValue = "10", required = false) Integer count) {
        if (count <= 0) {
            throw new IncorrectParameterException("Значение параметра count должно быть больше нуля");
        }
        List<Film> films = service.getPopularFilm(count);
        log.debug(String.format("Был выдан список %d популярных фильмов", count));
        return films;
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
