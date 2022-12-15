package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
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
}
