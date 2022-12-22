package ru.yandex.practicum.filmorate.storage;


import ru.yandex.practicum.filmorate.model.Film;


import java.util.List;


public interface FilmStorage {

    Film add(Film film);

    void remove(Integer id);

    Film get(Integer id);

    Film update(Film film);

    List<Film> getAll();

    void addLike(Integer id, Integer idUser);

    void removeLike(Integer id, Integer idUser);

    List<Film> getPopularFilm(Integer count);

    List<Film> getFilmsByDirector(Integer directorId, String sortBy);

    void isContains(Integer id);

    List<Film> getUserRecommendations(Integer id);
}
