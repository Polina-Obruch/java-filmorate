package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaService service;

    @GetMapping("/{id}")
    public Mpa getMpa(@PathVariable Integer id) {
        Mpa saveMpa = service.getMpa(id);
        log.debug(String.format("MPA с id = %d был выдан", id));
        return saveMpa;
    }

    @GetMapping
    public List<Mpa> getAllMpa() {
        List<Mpa> mpas = service.getAllMpa();
        log.debug("Список всех MPA был выдан");
        return mpas;
    }

}
