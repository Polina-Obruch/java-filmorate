package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.FilmUpdateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;


@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private Integer countId;

    @Autowired
    public FilmService (FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
        this.countId = 0;
    }

    public Film addFilm(Film film) {
        Integer id = getId();
        film.setId(id);
        filmStorage.add( id, film);
        return film;
    }

    public Film updateFilm(Film film) {
        Integer id = film.getId();

        if (filmStorage.get(id) != null) {
            filmStorage.add(id, film);
            return film;
        }

        log.debug("Фильм не может быть обновлен, так как отсутвтвует в базе данных");
        throw new FilmUpdateException();
    }

    public List<Film> getFilms() {
        return filmStorage.getAll();
    }

    private Integer getId() {
        return ++countId;
    }
}
