package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage storage;

    public Genre getGenre(Integer id) {
        log.debug(String.format("Выдача Genre c id = %d", id));
        return storage.getGenre(id);
    }

    public List<Genre> getAllGenre() {
        log.debug("Выдача всех Genre");
        return storage.getAllGenre();
    }

    public void setFilmGenre(Film film) {
        log.debug("Сохранение жанров фильма");
        storage.setFilmGenre(film);
    }

    public Film loadFilmGenre(Film film) {
        log.debug("Загрузка жанров для фильма");
        return storage.loadFilmGenre(film);
    }

    public List<Film> loadFilmsGenre(List<Film> films) {
        log.debug("Загрузка жанров для нескольких фильмов");

        //Если фильмов нет, то и загрузка жанров не нужна
        if (films.isEmpty()) {
            return films;
        }
        return storage.loadFilmsGenre(films);
    }
}
