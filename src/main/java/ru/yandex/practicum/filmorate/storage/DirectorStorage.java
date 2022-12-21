package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface DirectorStorage {

    Director getDirectorById(int id);

    List<Director> getAllDirectors();

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void removeDirector(int id);

    void setFilmDirector(Film film);

    Film loadFilmDirector(Film film);

    List<Film> loadFilmsDirector(List<Film> film);

    void isContains(Integer id);

}
