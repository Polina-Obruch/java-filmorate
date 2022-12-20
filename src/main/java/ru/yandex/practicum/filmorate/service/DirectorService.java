package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Director getDirector(Integer id) {
        log.debug(String.format("Получение режиссёра c id = %d", id));
        return directorStorage.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        log.debug("Добавление режиссёра");
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        Integer id = director.getId();
        log.debug(String.format("Обновление режиссёра c id = %d", id));
        return directorStorage.redactDirector(director);
    }

    public void setFilmDirector(Film film) {
        log.debug("Сохранение режиссёров фильма");
        directorStorage.setFilmDirector(film);
    }

    public void removeDirector(Integer id) {
        log.debug(String.format("Удаление режиссёра c id = %d", id));
        directorStorage.removeDirector(id);
    }

    public List<Director> getDirectors() {
        log.debug("Выдача всех режиссёров");
        return directorStorage.getAllDirectors();
    }

    public Film loadFilmDirector(Film film) {
        log.debug("Загрузка режиссёров для фильма");
        return directorStorage.loadFilmDirector(film);
    }

    public List<Film> loadFilmsDirector(List<Film> films) {
        log.debug("Загрузка режиссёров для нескольких фильмов");

        //Если фильмов нет, то и загрузка режиссёров не нужна
        if (films.isEmpty()) {
            return films;
        }
        return directorStorage.loadFilmsDirector(films);
    }

}
