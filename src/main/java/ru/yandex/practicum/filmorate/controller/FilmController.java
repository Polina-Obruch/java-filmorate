package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmUpdateException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService service = new FilmService();
    private int count;

    @GetMapping
    public List<Film> getFilms() {
        log.debug("Выданы все фильмы");
        return service.getFilms();
    }

    @PostMapping
    public Film addFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        validate(bindingResult);
        int id = getId();
        Film saveFilm = service.addFilm(id,film);
        log.debug("Новый фильм добавлен. Выданный id = " + id);
        return saveFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        validate(bindingResult);

        int id = film.getId();

        //Не отправляем данные сервису, пока не убедимся в необходимости этого
        if (!service.isContains(id)) {
            log.debug("Фильм не может быть обновлен, так как отсутвтвует в базе данных");
            throw new FilmUpdateException();
        }

        service.updateFilm(id,film);
        log.debug("Фильм с id = " + id + " был обновлен");
        return film;
    }

    private int getId() {
        return ++count;
    }

    //Для подробной записи ошибок в лог
    private void validate(BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("name")) {
            log.debug("Ошибка валидации фильма. Название фильма не может быть пустым");
            throw new FilmValidationException();
        }

        if (bindingResult.hasFieldErrors("description")) {
            log.debug("Ошибка валидации фильма. Описание не может превышать 200 символов");
            throw new FilmValidationException();
        }

        if (bindingResult.hasFieldErrors("releaseDate")) {
            log.debug("Ошибка валидации фильма. Дата релиза не может быть раньше 28 декабря 1895 года");
            throw new FilmValidationException();
        }

        if (bindingResult.hasFieldErrors("duration")) {
            log.debug("Ошибка валидации фильма. Продолжительность фильма не может быть отрицательным");
            throw new FilmValidationException();
        }
    }
}
