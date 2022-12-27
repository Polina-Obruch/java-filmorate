package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
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
    private final FilmService filmService;

    @GetMapping
    public List<Film> getFilms() {
        List<Film> films = filmService.getFilms();
        log.debug("Список всех фильмов был выдан");
        return films;
    }

    @PostMapping
    public Film addFilm(@RequestBody @Valid Film film) {
        Film saveFilm = filmService.addFilm(film);
        log.debug(String.format("Новый фильм был добавлен. Выданный id = %d", saveFilm.getId()));
        return saveFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film) {
        Film saveFilm = filmService.updateFilm(film);
        log.debug(String.format("Фильм с id = %d был обновлен", saveFilm.getId()));
        return saveFilm;
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Integer id) {
        Film saveFilm = filmService.getFilm(id);
        log.debug(String.format("Фильм с id = %d был выдан", saveFilm.getId()));
        return saveFilm;
    }

    @DeleteMapping("/{id}")
    public void removeFilm(@PathVariable Integer id) {
        filmService.removeFilm(id);
        log.debug(String.format("Фильм с id = %d удален", id));
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
        log.debug(String.format("Фильму с id = %d был поставлен лайк", id));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeLike(id, userId);
        log.debug(String.format("У фильма с id = %d был удален лайк", id));
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilm(@RequestParam(defaultValue = "10", required = false) Integer count,
                                     @RequestParam(required = false) Integer genreId,
                                     @RequestParam(required = false) Integer year) {
        if (count <= 0) {
            throw new IncorrectParameterException("Значение параметра count должно быть больше нуля");
        } else if (genreId != null && (genreId <= 0 || genreId >= 7)) {
            throw new IncorrectParameterException("Значение параметра genreId должно быть от 1 до 6");
        }
        List<Film> films = filmService.getPopularFilm(count, genreId, year);
        log.debug(String.format("Был выдан список %d популярных фильмов", count));
        return films;
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getDirectorFilm(@PathVariable Integer directorId,
                                      @RequestParam(defaultValue = "year", required = false) String sortBy) {
        if (!(sortBy.equals("year".toLowerCase()) || sortBy.equals("likes".toLowerCase()))) {
            throw new IncorrectParameterException("Значение параметра sortBy должно быть \"year\" или \"likes\"");
        }
        List<Film> films = filmService.getDirectorFilm(directorId, sortBy);
        if (films.size() == 0) {
            throw new FilmNotFoundException("Фильмов от этого режиссёра не найдено.");
        }
        log.debug(String.format("Был выдан список режиссёра %d, отсортированный по значению %s", directorId, sortBy));
        return films;
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam Integer userId, @RequestParam Integer friendId) {
        List<Film> films = filmService.getCommonFilms(userId, friendId);
        log.debug(String.format("Был выден список общих фильмов у пользователей с id %d и %d", userId, friendId));
        return films;
    }

    @GetMapping("/search")
    public List<Film> getSearchedFilms(@RequestParam String query, @RequestParam String by) {
        if (by.equals("title,director") || by.equals("director")
                || by.equals("title") || by.equals("director,title")) {
            List<Film> films = filmService.getSearchedFilms(query, by);
            log.debug(String.format("Был выдан список фильмов с поиском %s по значениям %s", query, by));
            return films;
        } else {
            throw new IncorrectParameterException("Неверное введены параметры поиска");
        }
    }
}
