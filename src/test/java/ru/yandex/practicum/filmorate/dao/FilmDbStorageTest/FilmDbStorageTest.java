package ru.yandex.practicum.filmorate.dao.FilmDbStorageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.DuplicateLikeException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
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
public class FilmDbStorageTest {
    private final FilmStorage filmDbStorage;
    private final UserStorage userDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final JdbcTemplate jdbcTemplate;


    @BeforeEach
    public void addFilm() {

        Film film1 = createFilm("film1", "char1", "2000-12-27",
                1, new Mpa(1, "G"),
                new LinkedHashSet<>(List.of(new Genre(1, "Комедия")))
        );
        filmDbStorage.add(film1);
        genreDbStorage.setFilmGenre(film1);

        Film film2 = createFilm("film2", "char2", "2001-11-25",
                2, new Mpa(1, "G"),
                new LinkedHashSet<>()
        );
        filmDbStorage.add(film2);
        genreDbStorage.setFilmGenre(film2);

        Film film3 = createFilm("film3", "char3", "2002-05-06",
                3, new Mpa(1, "G"),
                new LinkedHashSet<>(List.of(new Genre(1, "Комедия"),
                        new Genre(2, "Драма")))
        );
        filmDbStorage.add(film3);
        genreDbStorage.setFilmGenre(film3);
    }

    @AfterEach
    public void clearDB() {
        System.out.println("Удаляем данные");
        String sqlQuery = "DROP TABLE FRIENDS ";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "DROP TABLE FILMS_LIKES";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "DROP TABLE FILMS_GENRE ";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "DROP TABLE USERS ";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "DROP TABLE FILMS ";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "create table IF NOT EXISTS USERS (" +
                "    USER_ID    INTEGER AUTO_INCREMENT," +
                "    USER_EMAIL VARCHAR not null," +
                "    USER_NAME  VARCHAR not null," +
                "    USER_LOGIN VARCHAR not null," +
                "    BIRTHDAY   DATE    not null," +
                "    constraint USERS_PK primary key (USER_ID));" ;
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "create table IF NOT EXISTS FILMS(" +
                "    FILM_ID          INTEGER AUTO_INCREMENT," +
                "    FILM_NAME        VARCHAR      not null," +
                "    FILM_DESCRIPTION VARCHAR(200) not null," +
                "    RELEASE_DATE     DATE         not null," +
                "    DURATION         INTEGER      not null," +
                "    MPA_ID           INTEGER      not null," +
                "    LIKES            INTEGER DEFAULT 0," +
                "    constraint FILMS_PK primary key (FILM_ID)," +
                "    constraint FILMS_MPA_null_fk foreign key (MPA_ID) references MPA);";
        jdbcTemplate.update(sqlQuery);


        sqlQuery = "create table IF NOT EXISTS FILMS_LIKES( " +
                "FILM_ID INTEGER not null," +
                "USER_ID INTEGER not null," +
                "constraint uq_likes UNIQUE (FILM_ID, USER_ID)," +
                "constraint FILMS_LIKES_fk foreign key (FILM_ID) references FILMS ON DELETE CASCADE," +
                "constraint FILMS_LIKES_USER_fk foreign key (USER_ID) references USERS ON DELETE CASCADE);";
        jdbcTemplate.update(sqlQuery);


        sqlQuery = "create table IF NOT EXISTS FRIENDS( " +
                "USER_ID   INTEGER not null," +
                "FRIEND_ID INTEGER not null," +
                "constraint USER_FRIENDS_fk foreign key (USER_ID) references USERS ON DELETE CASCADE," +
                "constraint FRIEND_USER_fk  foreign key (FRIEND_ID) references USERS (USER_ID) ON DELETE CASCADE);";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "create table IF NOT EXISTS FILMS_GENRE(" +
                "    FILM_ID  INTEGER not null," +
                "    GENRE_ID INTEGER not null," +
                "    constraint FILMS_GENRE_fk foreign key (FILM_ID) references FILMS ON DELETE CASCADE," +
                "    constraint FILMS_GENRE_GENRE_null_fk foreign key (GENRE_ID) references GENRE ON DELETE CASCADE);";
        jdbcTemplate.update(sqlQuery);
    }

    @Test
    public void getFilmById() {
        Film film1 = genreDbStorage.loadFilmGenre(filmDbStorage.get(1));

        assertEquals(1, film1.getId());
        assertEquals("film1", film1.getName());
        assertEquals("char1", film1.getDescription());
        assertEquals(LocalDate.parse("2000-12-27"), film1.getReleaseDate());
        assertEquals(1, film1.getDuration());
        assertEquals(1, film1.getMpa().getId());
        assertEquals("G", film1.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "Комедия"))),
                film1.getGenres());

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
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "Комедия"),
                new Genre(2, "Драма"))), film3.getGenres());
    }

    @Test
    public void removeFilmById() {
        filmDbStorage.remove(1);

        final FilmNotFoundException exp = assertThrows(FilmNotFoundException.class,
                () -> filmDbStorage.get(1));
        assertEquals("Фильм с id = 1 не найден в базе", exp.getMessage());
    }

    @Test
    public void updateFilmById() {
        Film film1 = genreDbStorage.loadFilmGenre(filmDbStorage.get(1));
        film1.setName("Update film1");
        filmDbStorage.update(film1);
        genreDbStorage.setFilmGenre(film1);

        Film filmUpdate = genreDbStorage.loadFilmGenre(filmDbStorage.get(1));
        assertEquals(1, filmUpdate.getId());
        assertEquals("Update film1", filmUpdate.getName());
        assertEquals("char1", filmUpdate.getDescription());
        assertEquals(LocalDate.parse("2000-12-27"), filmUpdate.getReleaseDate());
        assertEquals(1, filmUpdate.getDuration());
        assertEquals(1, filmUpdate.getMpa().getId());
        assertEquals("G", filmUpdate.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "Комедия"))),
                film1.getGenres());

    }

    @Test
    public void getAllFilms() {
        List<Film> films = genreDbStorage.loadFilmsGenre(filmDbStorage.getAll());
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
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "Комедия"))),
                film1.getGenres());

        assertEquals(2, film2.getId());
        assertEquals("film2", film2.getName());
        assertEquals("char2", film2.getDescription());
        assertEquals(LocalDate.parse("2001-11-25"), film2.getReleaseDate());
        assertEquals(2, film2.getDuration());
        assertEquals(1, film2.getMpa().getId());
        assertEquals("G", film2.getMpa().getName());
        assertEquals(new LinkedHashSet<>(), film2.getGenres());

        assertEquals(3, film3.getId());
        assertEquals("film3", film3.getName());
        assertEquals("char3", film3.getDescription());
        assertEquals(LocalDate.parse("2002-05-06"), film3.getReleaseDate());
        assertEquals(3, film3.getDuration());
        assertEquals(1, film3.getMpa().getId());
        assertEquals("G", film3.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "Комедия"),
                new Genre(2, "Драма"))), film3.getGenres());

    }

    @Test
    public void operationWithLikes() {
        userDbStorage.add(createUser("mail@mail.ru", "Nick Name", "name", "1990-08-20"));
        userDbStorage.add(createUser("yandex@yandex.ru", "Mr Bin", "Bin", "1991-11-23"));
        userDbStorage.add(createUser("rim@mail.ru", "Rim", "Rimus", "1992-07-21"));

        //Добавление лайка
        filmDbStorage.addLike(1, 1);

        final DuplicateLikeException exp = assertThrows(DuplicateLikeException.class,
                () -> filmDbStorage.addLike(1, 1));
        assertEquals("Лайк фильму с id = 1 от пользователя с id = 1 уже был поставлен", exp.getMessage());

        filmDbStorage.removeLike(1, 1);

    }

    @Test
    public void getPopularFilm() {
        userDbStorage.add(createUser("mail@mail.ru", "Nick Name", "name", "1990-08-20"));
        userDbStorage.add(createUser("yandex@yandex.ru", "Mr Bin", "Bin", "1991-11-23"));
        userDbStorage.add(createUser("rim@mail.ru", "Rim", "Rimus", "1992-07-21"));

        //Значение по дефолту - выдаем все 3 фильма - у всех 0 лайков
        List<Film> films = genreDbStorage.loadFilmsGenre(filmDbStorage.getPopularFilm(10));
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
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "Комедия"))),
                film1.getGenres());

        assertEquals(2, film2.getId());
        assertEquals("film2", film2.getName());
        assertEquals("char2", film2.getDescription());
        assertEquals(LocalDate.parse("2001-11-25"), film2.getReleaseDate());
        assertEquals(2, film2.getDuration());
        assertEquals(1, film2.getMpa().getId());
        assertEquals("G", film2.getMpa().getName());
        assertEquals(new LinkedHashSet<>(), film2.getGenres());

        assertEquals(3, film3.getId());
        assertEquals("film3", film3.getName());
        assertEquals("char3", film3.getDescription());
        assertEquals(LocalDate.parse("2002-05-06"), film3.getReleaseDate());
        assertEquals(3, film3.getDuration());
        assertEquals(1, film3.getMpa().getId());
        assertEquals("G", film3.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "Комедия"),
                new Genre(2, "Драма"))), film3.getGenres());

        //Добавление лайка
        filmDbStorage.addLike(2, 1);

        //1 популярный фильм - выдаем 1 фильм - у него 1 лайк
        films = genreDbStorage.loadFilmsGenre(filmDbStorage.getPopularFilm(1));
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

        filmDbStorage.removeLike(2, 1);

        //1 популярный фильм - выдаем 1 фильм - у всех 0 лайков - выдаем по самому маленькому id
        films = genreDbStorage.loadFilmsGenre(filmDbStorage.getPopularFilm(1));
        assertEquals(1, films.size());

        film1 = films.get(0);
        assertEquals(1, film1.getId());
        assertEquals("film1", film1.getName());
        assertEquals("char1", film1.getDescription());
        assertEquals(LocalDate.parse("2000-12-27"), film1.getReleaseDate());
        assertEquals(1, film1.getDuration());
        assertEquals(1, film1.getMpa().getId());
        assertEquals("G", film1.getMpa().getName());
        assertEquals(new LinkedHashSet<>(List.of(new Genre(1, "Комедия"))),
                film1.getGenres());

    }


    private Film createFilm(String name, String description, String releaseDate, int duration, Mpa mpa, LinkedHashSet<Genre> genres) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.parse(releaseDate));
        film.setDuration(duration);
        film.setMpa(mpa);
        film.setGenres(genres);
        return film;
    }
}
