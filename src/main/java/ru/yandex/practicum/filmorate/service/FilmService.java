package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final FeedService feedService;

    public Film getFilm(Integer id) {
        log.debug(String.format("Выдача фильма с id = %d", id));
        Film film = genreService.loadFilmGenre(filmStorage.get(id));
        return directorService.loadFilmDirector(film);
    }

    public Film addFilm(Film film) {
        log.debug("Сохранение фильма");

        //Если придет фильм без поля genres или directors - инициализируем пустым списком
        // для искл. ошибки NullPointerException при обращении к этим полям
        genreAndDirectorCheck(film);
        Film saveFilm = filmStorage.add(film);
        genreService.setFilmGenre(saveFilm);
        directorService.setFilmDirector(saveFilm);
        return saveFilm;
    }

    public Film updateFilm(Film film) {
        log.debug(String.format("Обновление фильма с id = %d", film.getId()));
        genreAndDirectorCheck(film);
        Film updateFilm = filmStorage.update(film);
        genreService.setFilmGenre(updateFilm);
        directorService.setFilmDirector(updateFilm);
        return updateFilm;
    }

    private void genreAndDirectorCheck(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }
        if (film.getDirectors() == null) {
            film.setDirectors(new LinkedHashSet<>());
        }
        for (Director d : film.getDirectors()) {
            isDirectorContains(d.getId());
        }
    }

    public void removeFilm(Integer id) {
        log.debug(String.format("Удаляем фильм с id =%d", id));
        filmStorage.remove(id);
    }

    public List<Film> getFilms() {
        log.debug("Выдача списка всех фильмов");
        List<Film> films = genreService.loadFilmsGenre(filmStorage.getAll());
        return directorService.loadFilmsDirector(films);
    }

    public void addLike(Integer id, Integer idUser) {
        log.debug(String.format("Добавление лайка фильму с id = %d от пользователя с id = %d", id, idUser));
        isFilmContains(id);
        userService.isContainsUser(idUser);
        feedService.saveEventAddLikeFilm(id, idUser);
        filmStorage.addLike(id, idUser);
    }

    public void removeLike(Integer id, Integer idUser) {
        log.debug(String.format("Уаление лайка у фильма с id = %d от пользователя с id = %d", id, idUser));
        isFilmContains(id);
        userService.isContainsUser(idUser);
        filmStorage.removeLike(id, idUser);
        feedService.saveEventRemoveLikeFilm(id, idUser);
    }

    public List<Film> getPopularFilm(Integer count, Integer genreId, Integer year) {
        String genreName;
        if (count <= 0) {
            throw new IncorrectParameterException("Значение параметра count должно быть больше нуля");
        } else if (genreId != null && (genreId <= 0 || genreId >= 7)) {
            throw new IncorrectParameterException("Значение параметра genreId должно быть от 1 до 6");
        } else if (year != null && genreId != null) {
            genreName = genreService.getGenre(genreId).getName();
            log.debug(String.format("Выдача списка %d популярных фильмов в жанре %s %d года", count, genreName, year));
        } else if (year == null && genreId != null) {
            genreName = genreService.getGenre(genreId).getName();
            log.debug(String.format("Выдача списка %d популярных фильмов в жанре %s", count, genreName));
        } else if (year != null) {
            log.debug(String.format("Выдача списка %d популярных фильмов %d года", count, year));
        } else {
            log.debug(String.format("Выдача списка %d популярных фильмов", count));
        }
        return genreService.loadFilmsGenre(filmStorage.getPopularFilm(count, genreId, year));
    }

    public List<Film> getDirectorFilm(int directorId, String sortBy) {
        if (!(sortBy.equals("year".toLowerCase()) || sortBy.equals("likes".toLowerCase()))) {
            throw new IncorrectParameterException("Значение параметра sortBy должно быть \"year\" или \"likes\"");
        }
        isDirectorContains(directorId);

        log.debug(String.format("Выдача списка фильмов режиссёра %d отсортированных по критерию %s", directorId, sortBy));
        List<Film> films = genreService.loadFilmsGenre(filmStorage.getFilmsByDirector(directorId, sortBy.toLowerCase()));
        films = directorService.loadFilmsDirector(films);
        if (films.size() == 0) {
            throw new EntityNotFoundException("Фильмов от этого режиссёра не найдено.");
        }
        return films;
    }

    public void isFilmContains(Integer id) {
        filmStorage.isContains(id);
    }

    private void isDirectorContains(Integer id) {
        directorService.isContains(id);
    }

    public List<Film> getUserRecommendations(Integer id) {
        log.debug(String.format("Выдача рекомендованных фильмов для пользователя %d", id));
        userService.isContainsUser(id);
        return giveFilmsGenresAndDirector(filmStorage.getUserRecommendations(id));
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        log.debug(String.format("Выдача списка общих фильмов у пользователей с id %d и %d", userId, friendId));
        userService.isContainsUser(userId);
        userService.isContainsUser(friendId);
        return giveFilmsGenresAndDirector(filmStorage.getCommonFilms(userId, friendId));
    }

    public List<Film> getSearchedFilms(String query, String by) {
        log.debug(String.format("Выдача списка фильмов с поиском %s по %s", query, by));
        if (by.equals("title,director") || by.equals("director")
                || by.equals("title") || by.equals("director,title")) {
            return giveFilmsGenresAndDirector(new ArrayList<>(filmStorage.getSearchedFilms(query, by)));
        } else {
            throw new IncorrectParameterException("Неверно введены параметры поиска");
        }
    }

    private List<Film> giveFilmsGenresAndDirector(List<Film> films) {
        return directorService.loadFilmsDirector(genreService.loadFilmsGenre(films));
    }
}
