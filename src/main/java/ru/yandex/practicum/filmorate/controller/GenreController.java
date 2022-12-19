package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService service;

    @GetMapping("/{id}")
    public Genre getGenre(@PathVariable Integer id) {
        Genre saveGenre = service.getGenre(id);
        log.debug(String.format("Genre с id = %d был выдан", id));
        return saveGenre;
    }

    @GetMapping
    public List<Genre> getAllGenre() {
        List<Genre> genres = service.getAllGenre();
        log.debug("Список всех Genre был выдан");
        return genres;
    }

}
