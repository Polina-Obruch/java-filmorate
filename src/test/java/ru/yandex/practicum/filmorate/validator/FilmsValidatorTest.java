package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.Set;

public class FilmsValidatorTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Проверка невалидности name")
    void addFilmWithFailName() {
        Film film = createFilm(" ", "char", "2000-02-20", 5);
        Film film1 = createFilm(null, "char", "2000-02-20", 5);

        Assertions.assertAll(
                () -> Assertions.assertTrue(requestFilm(film, "Название фильма не может быть пустым")),
                () -> Assertions.assertTrue(requestFilm(film1, "Название фильма не может быть пустым"))
        );
    }

    @Test
    @DisplayName("Проверка невалидности description")
    void addFilmWithFailDescription() {
        Film film = createFilm("film", new String(new char[200]), "2000-02-20", 5);
        Film film1 = createFilm("film1", new String(new char[201]), "2000-02-20", 5);

        Assertions.assertAll(
                () -> Assertions.assertFalse(requestFilm(film, "Описание не может превышать 200 символов")),
                () -> Assertions.assertTrue(requestFilm(film1, "Описание не может превышать 200 символов"))
        );
    }

    @Test
    @DisplayName("Проверка невалидности releaseDate")
    void addFilmWithFailReleaseData() {
        Film film = createFilm("film", "char", "1895-12-27", 5);
        Assertions.assertTrue(requestFilm(film, "Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    private Film createFilm(String name, String description, String releaseDate, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.parse(releaseDate));
        film.setDuration(duration);
        return film;
    }

    private boolean requestFilm(Film film, @NotEmpty String message) {
        Set<ConstraintViolation<Film>> errors = VALIDATOR.validate(film);
        return errors.stream().map(ConstraintViolation::getMessage).anyMatch(message::equals);
    }
}
