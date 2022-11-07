package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmUpdateException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int count;

    @GetMapping
    public List<Film> getFilms() {
        log.debug("Выданы все фильмы");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        validate(bindingResult);
        int id = getId();
        film.setId(id);
        films.put(id, film);
        log.debug("Новый фильм добавлен. Выданный id = " + id);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        validate(bindingResult);

        int id = film.getId();
        if (!films.containsKey(id)) {
            log.debug("Фильм не может быть обновлен, так как отсутвтвует в базе данных");
            throw new FilmUpdateException();
        }

        films.put(id, film);
        log.debug("Фильм с id = " + id + " был обновлен");
        return film;
    }

    private int getId() {
        return ++count;
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
