package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService service;

    @GetMapping
    public List<Director> getAllDirectors() {
        return service.getDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable int id) {
        return service.getDirector(id);
    }

    @PostMapping
    public Director createDirector(@RequestBody @Valid Director director) {
        return service.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody @Valid Director director) {
        return service.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable int id) {
        service.removeDirector(id);
    }
}
