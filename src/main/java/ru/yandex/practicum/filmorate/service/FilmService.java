package ru.yandex.practicum.filmorate.service;


import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilmService {
    private final Map<Integer, Film> films = new HashMap<>();

    public Film addFilm(int id, Film film) {
        film.setId(id);
        films.put(id, film);
        return film;
    }

    public Film updateFilm(int id, Film film) {
        films.put(id, film);
        return film;
    }

    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    public boolean isContains(int id) {
        return films.containsKey(id);
    }
}
