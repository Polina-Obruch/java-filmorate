package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private Integer countId;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService service) {
        this.filmStorage = filmStorage;
        this.userService = service;
        this.countId = 0;
    }

    public Film getFilm(Integer id) {
        log.debug(String.format("Выдача фильма с id = %d", id));
        isFilmContains(id);
        return filmStorage.get(id);
    }

    public Film addFilm(Film film) {
        Integer id = getId();
        log.debug(String.format("Сохранение фильма с id = %d", id));

        film.setId(id);

        //Если придет фильм без поля likes - инициализируем пустым списком
        // для искл. ошибки NullPointerException при обращении к этому полю
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        filmStorage.add(id, film);
        return film;
    }

    public Film updateFilm(Film film) {
        Integer id = film.getId();

        log.debug(String.format("Обновление фильма с id = %d", id));
        isFilmContains(id);

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        filmStorage.add(id, film);
        return film;
    }

    public List<Film> getFilms() {
        log.debug("Выдача списка всех фильмов");
        return filmStorage.getAll();
    }

    public void addLike(Integer id, Integer userId) {
        log.debug(String.format("Добавление лайка фильму с id = %d от пользователя с id = %d", id, userId));
        isFilmContains(id);
        userService.isContainsUser(userId);
        filmStorage.get(id).getLikes().add(userId);
    }

    public void removeLike(Integer id, Integer userId) {
        log.debug(String.format("Уаление лайка с id = %d от пользователя с id = %d", id, userId));
        isFilmContains(id);
        userService.isContainsUser(userId);
        filmStorage.get(id).getLikes().remove(userId);
    }

    public List<Film> getPopularFilm(Integer count) {
        log.debug(String.format("Выдача списка %d популярных фильмов", count));
        return filmStorage.getAll().stream()
                .sorted((film1, film2) -> film2.getLikes().size() - film1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    private void isFilmContains(Integer id) {
        if (!filmStorage.isContains(id)) {
            log.debug(String.format("Фильм с id = %d не был найден в базе", id));
            throw new FilmNotFoundException(String.format("Фильм с id = %d не найден в базе", id));
        }
    }

    private Integer getId() {
        return ++countId;
    }
}
