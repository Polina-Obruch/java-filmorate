package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage storage;

    public Mpa getMpa(Integer id) {
        log.debug(String.format("Выдача MPA c id = %d", id));
        return storage.getMpa(id);
    }

    public List<Mpa> getAllMpa() {
        log.debug("Выдача всех MPA");
        return storage.getAllMpa();
    }
}
