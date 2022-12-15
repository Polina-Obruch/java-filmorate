package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.LinkedHashSet;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;


    public Film getFilm(Integer id) {
        log.debug(String.format("Выдача фильма с id = %d", id));
        return genreService.loadFilmGenre(filmStorage.get(id));
    }

    public Film addFilm(Film film) {
        log.debug("Сохранение фильма");

        //Если придет фильм без поля genres - инициализируем пустым списком
        // для искл. ошибки NullPointerException при обращении к этому полю
        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }

        Film saveFilm = filmStorage.add(film);
        genreService.setFilmGenre(saveFilm);
        return saveFilm;
    }

    public Film updateFilm(Film film) {
        log.debug(String.format("Обновление фильма с id = %d", film.getId()));

        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }
        Film updateFilm = filmStorage.update(film);
        genreService.setFilmGenre(updateFilm);
        return updateFilm;
    }

    public void removeFilm(Integer id) {
        log.debug(String.format("Удаляем фильм с id =%d", id));
        filmStorage.remove(id);
    }

    public List<Film> getFilms() {
        log.debug("Выдача списка всех фильмов");
        return genreService.loadFilmsGenre(filmStorage.getAll());
    }

    public void addLike(Integer id, Integer idUser) {
        log.debug(String.format("Добавление лайка фильму с id = %d от пользователя с id = %d", id, idUser));
        isFilmContains(id);
        userService.isContainsUser(idUser);
        filmStorage.addLike(id, idUser);
    }

    public void removeLike(Integer id, Integer idUser) {
        log.debug(String.format("Уаление лайка у фильма с id = %d от пользователя с id = %d", id, idUser));
        isFilmContains(id);
        userService.isContainsUser(idUser);
        filmStorage.removeLike(id, idUser);
    }

    public List<Film> getPopularFilm(Integer count) {
        log.debug(String.format("Выдача списка %d популярных фильмов", count));
        return genreService.loadFilmsGenre(filmStorage.getPopularFilm(count));
    }

    private void isFilmContains(Integer id) {
        filmStorage.isContains(id);
    }
}
