package ru.yandex.practicum.filmorate.dao.FilmDbStorageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.DuplicateLikeException;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.filmorate.dao.UserDbStorageTest.UserDbStorageTest.createUser;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FilmDbStorageTest {

    private final FilmStorage filmDbStorage;
    private final UserStorage userDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final DirectorDbStorage directorDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void addFilm() {
        String sqlQuery = "INSERT INTO DIRECTORS(DIRECTOR_NAME) "
                + "VALUES(?)";
        jdbcTemplate.update(sqlQuery, "Director");

        Film film1 = createFilm("film1", "char1", "2000-12-27",
                1, new Mpa(1, "G"),
                new LinkedHashSet<>(List.of(new Genre(1, "??????????????"))),
                new LinkedHashSet<>(List.of(new Director(1, "Director")))
        );
        filmDbStorage.add(film1);
        genreDbStorage.setFilmGenre(film1);
        directorDbStorage.setFilmDirector(film1);

        Film film2 = createFilm("film2", "char2", "2001-11-25",
                2, new Mpa(1, "G"),
                new LinkedHashSet<>(),
                new LinkedHashSet<>()
        );
        filmDbStorage.add(film2);
        genreDbStorage.setFilmGenre(film2);

        Film film3 = createFilm("film3", "char3", "2002-05-06",
                3, new Mpa(1, "G"),
                new LinkedHashSet<>(
                        List.of(new Genre(1, "??????????????"), new Genre(2, "??????????"))),
                new LinkedHashSet<>()
        );
        filmDbStorage.add(film3);
        genreDbStorage.setFilmGenre(film3);
    }

    @Test
    public void getFilmById() {
        Film film1 = directorDbStorage.loadFilmDirector(genreDbStorage.loadFilmGenre(filmDbStorage.get(1)));

        assertEquals(1, film1.getId());
        assertEquals("film1", film1.getName());
        assertEquals("char1", film1.getDescription());
        assertEquals(LocalDate.parse("2000-12-27"), film1.getReleaseDate());
        assertEquals(1, film1.getDuration());
        assertEquals(1, film1.getMpa().getId());
        assertEquals("G", film1.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "??????????????"))),
                film1.getGenres());
        assertEquals(new LinkedHashSet<>(List.of(new Director(1, "Director"))),
                film1.getDirectors());

        Film film2 = genreDbStorage.loadFilmGenre(filmDbStorage.get(2));

        assertEquals(2, film2.getId());
        assertEquals("film2", film2.getName());
        assertEquals("char2", film2.getDescription());
        assertEquals(LocalDate.parse("2001-11-25"), film2.getReleaseDate());
        assertEquals(2, film2.getDuration());
        assertEquals(1, film2.getMpa().getId());
        assertEquals("G", film2.getMpa().getName());
        assertEquals(new LinkedHashSet<>(), film2.getGenres());

        Film film3 = genreDbStorage.loadFilmGenre(filmDbStorage.get(3));

        assertEquals(3, film3.getId());
        assertEquals("film3", film3.getName());
        assertEquals("char3", film3.getDescription());
        assertEquals(LocalDate.parse("2002-05-06"), film3.getReleaseDate());
        assertEquals(3, film3.getDuration());
        assertEquals(1, film3.getMpa().getId());
        assertEquals("G", film3.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "??????????????"),
                new Genre(2, "??????????"))), film3.getGenres());
    }

    @Test
    public void removeFilmById() {
        filmDbStorage.remove(1);

        final EntityNotFoundException exp = assertThrows(EntityNotFoundException.class,
                () -> filmDbStorage.get(1));
        assertEquals("?????????? ?? id = 1 ???? ???????????? ?? ????????", exp.getMessage());
    }

    @Test
    public void updateFilmById() {
        Film film1 = directorDbStorage.loadFilmDirector(genreDbStorage.loadFilmGenre(filmDbStorage.get(1)));
        film1.setName("Update film1");
        filmDbStorage.update(film1);
        genreDbStorage.setFilmGenre(film1);
        directorDbStorage.setFilmDirector(film1);

        Film filmUpdate = directorDbStorage.loadFilmDirector(genreDbStorage.loadFilmGenre(filmDbStorage.get(1)));
        assertEquals(1, filmUpdate.getId());
        assertEquals("Update film1", filmUpdate.getName());
        assertEquals("char1", filmUpdate.getDescription());
        assertEquals(LocalDate.parse("2000-12-27"), filmUpdate.getReleaseDate());
        assertEquals(1, filmUpdate.getDuration());
        assertEquals(1, filmUpdate.getMpa().getId());
        assertEquals("G", filmUpdate.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "??????????????"))),
                film1.getGenres());
        assertEquals(new LinkedHashSet<>(List.of(new Director(1, "Director"))),
                film1.getDirectors());

    }

    @Test
    public void getAllFilms() {
        List<Film> films = directorDbStorage.loadFilmsDirector(
                genreDbStorage.loadFilmsGenre(filmDbStorage.getAll()));
        assertEquals(3, films.size());
        Film film1 = films.get(0);
        Film film2 = films.get(1);
        Film film3 = films.get(2);

        assertEquals(1, film1.getId());
        assertEquals("film1", film1.getName());
        assertEquals("char1", film1.getDescription());
        assertEquals(LocalDate.parse("2000-12-27"), film1.getReleaseDate());
        assertEquals(1, film1.getDuration());
        assertEquals(1, film1.getMpa().getId());
        assertEquals("G", film1.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "??????????????"))),
                film1.getGenres());
        assertEquals(new LinkedHashSet<>(List.of(new Director(1, "Director"))),
                film1.getDirectors());

        assertEquals(2, film2.getId());
        assertEquals("film2", film2.getName());
        assertEquals("char2", film2.getDescription());
        assertEquals(LocalDate.parse("2001-11-25"), film2.getReleaseDate());
        assertEquals(2, film2.getDuration());
        assertEquals(1, film2.getMpa().getId());
        assertEquals("G", film2.getMpa().getName());
        assertEquals(new LinkedHashSet<>(), film2.getGenres());
        assertEquals(new LinkedHashSet<>(), film2.getDirectors());

        assertEquals(3, film3.getId());
        assertEquals("film3", film3.getName());
        assertEquals("char3", film3.getDescription());
        assertEquals(LocalDate.parse("2002-05-06"), film3.getReleaseDate());
        assertEquals(3, film3.getDuration());
        assertEquals(1, film3.getMpa().getId());
        assertEquals("G", film3.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "??????????????"),
                new Genre(2, "??????????"))), film3.getGenres());
        assertEquals(new LinkedHashSet<>(), film3.getDirectors());

    }

    @Test
    public void operationWithLikes() {
        userDbStorage.add(createUser("mail@mail.ru", "Nick Name", "name", "1990-08-20"));
        userDbStorage.add(createUser("yandex@yandex.ru", "Mr Bin", "Bin", "1991-11-23"));
        userDbStorage.add(createUser("rim@mail.ru", "Rim", "Rimus", "1992-07-21"));

        //???????????????????? ??????????
        filmDbStorage.addLike(1, 1);

        final DuplicateLikeException exp = assertThrows(DuplicateLikeException.class,
                () -> filmDbStorage.addLike(1, 1));
        assertEquals("???????? ???????????? ?? id = 1 ???? ???????????????????????? ?? id = 1 ?????? ?????? ??????????????????", exp.getMessage());

        filmDbStorage.removeLike(1, 1);

    }

    @Test
    public void getPopularFilm() {
        userDbStorage.add(createUser("mail@mail.ru", "Nick Name", "name", "1990-08-20"));
        userDbStorage.add(createUser("yandex@yandex.ru", "Mr Bin", "Bin", "1991-11-23"));
        userDbStorage.add(createUser("rim@mail.ru", "Rim", "Rimus", "1992-07-21"));

        //???????????????? ???? ?????????????? - ???????????? ?????? 3 ???????????? - ?? ???????? 0 ????????????
        List<Film> films = directorDbStorage.loadFilmsDirector(
                genreDbStorage.loadFilmsGenre(filmDbStorage.getPopularFilm(10, null, null)));
        assertEquals(3, films.size());

        Film film1 = films.get(0);
        Film film2 = films.get(1);
        Film film3 = films.get(2);

        assertEquals(1, film1.getId());
        assertEquals("film1", film1.getName());
        assertEquals("char1", film1.getDescription());
        assertEquals(LocalDate.parse("2000-12-27"), film1.getReleaseDate());
        assertEquals(1, film1.getDuration());
        assertEquals(1, film1.getMpa().getId());
        assertEquals("G", film1.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "??????????????"))),
                film1.getGenres());
        assertEquals(new LinkedHashSet<>(List.of(new Director(1, "Director"))),
                film1.getDirectors());

        assertEquals(2, film2.getId());
        assertEquals("film2", film2.getName());
        assertEquals("char2", film2.getDescription());
        assertEquals(LocalDate.parse("2001-11-25"), film2.getReleaseDate());
        assertEquals(2, film2.getDuration());
        assertEquals(1, film2.getMpa().getId());
        assertEquals("G", film2.getMpa().getName());
        assertEquals(new LinkedHashSet<>(), film2.getGenres());
        assertEquals(new LinkedHashSet<>(), film2.getDirectors());

        assertEquals(3, film3.getId());
        assertEquals("film3", film3.getName());
        assertEquals("char3", film3.getDescription());
        assertEquals(LocalDate.parse("2002-05-06"), film3.getReleaseDate());
        assertEquals(3, film3.getDuration());
        assertEquals(1, film3.getMpa().getId());
        assertEquals("G", film3.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "??????????????"),
                new Genre(2, "??????????"))), film3.getGenres());
        assertEquals(new LinkedHashSet<>(), film3.getDirectors());

        //???????????????????? ??????????
        filmDbStorage.addLike(2, 1);

        //1 ???????????????????? ?????????? - ???????????? 1 ?????????? - ?? ???????? 1 ????????
        films = directorDbStorage.loadFilmsDirector(
                genreDbStorage.loadFilmsGenre(filmDbStorage.getPopularFilm(1, null, null)));
        assertEquals(1, films.size());
        film2 = films.get(0);

        assertEquals(2, film2.getId());
        assertEquals("film2", film2.getName());
        assertEquals("char2", film2.getDescription());
        assertEquals(LocalDate.parse("2001-11-25"), film2.getReleaseDate());
        assertEquals(2, film2.getDuration());
        assertEquals(1, film2.getMpa().getId());
        assertEquals("G", film2.getMpa().getName());
        assertEquals(new LinkedHashSet<>(), film2.getGenres());
        assertEquals(new LinkedHashSet<>(), film2.getDirectors());

        filmDbStorage.removeLike(2, 1);

        //1 ???????????????????? ?????????? - ???????????? 1 ?????????? - ?? ???????? 0 ???????????? - ???????????? ???? ???????????? ???????????????????? id
        films = directorDbStorage.loadFilmsDirector(
                genreDbStorage.loadFilmsGenre(filmDbStorage.getPopularFilm(1, null, null)));
        assertEquals(1, films.size());

        film1 = films.get(0);
        assertEquals(1, film1.getId());
        assertEquals("film1", film1.getName());
        assertEquals("char1", film1.getDescription());
        assertEquals(LocalDate.parse("2000-12-27"), film1.getReleaseDate());
        assertEquals(1, film1.getDuration());
        assertEquals(1, film1.getMpa().getId());
        assertEquals("G", film1.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "??????????????"))),
                film1.getGenres());
        assertEquals(new LinkedHashSet<>(List.of(new Director(1, "Director"))),
                film1.getDirectors());

    }

    private Film createFilm(String name, String description, String releaseDate, int duration, Mpa mpa,
                            LinkedHashSet<Genre> genres, LinkedHashSet<Director> directors) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.parse(releaseDate));
        film.setDuration(duration);
        film.setMpa(mpa);
        film.setGenres(genres);
        film.setDirectors(directors);
        return film;
    }
}
