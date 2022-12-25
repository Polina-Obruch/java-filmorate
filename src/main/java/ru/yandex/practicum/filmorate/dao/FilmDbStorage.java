package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.CountOfResultNotExpectedException;
import ru.yandex.practicum.filmorate.exception.DuplicateLikeException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {
        log.debug("Запрос к БД на сохранение фильма");
        String sqlQuery = "INSERT INTO FILMS(film_name, film_description, release_date, duration, mpa_id) "
                + "VALUES(?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        Integer id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        film.setId(id);
        return film;
    }

    @Override
    public void remove(Integer id) {
        log.debug("Запрос к БД на удаление");
        final String sqlQuery = "DELETE FROM FILMS " +
                "WHERE FILM_ID = ? ";

        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public Film get(Integer id) {
        final String sqlQuery = "SELECT *" +
                "FROM FILMS " +
                "INNER JOIN MPA M ON M.MPA_ID = FILMS.MPA_ID " +
                "WHERE FILM_ID = ? ";

        final List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm, id);

        if (films.size() == 0) {
            log.debug(String.format("Фильм с id = %d не был найден в базе", id));
            throw new FilmNotFoundException(String.format("Фильм с id = %d не найден в базе", id));
        }

        if (films.size() != 1) {
            throw new CountOfResultNotExpectedException("Количество полученных фильмов не совпадает с ожидаемым");
        }
        return films.get(0);
    }

    @Override
    public Film update(Film film) {
        log.debug("Запрос к БД на обновление фильма");
        int id = film.getId();

        final String sqlQuery = "UPDATE FILMS SET " +
                "film_name = ?, film_description = ?, release_date = ?, duration = ?, mpa_id = ?" +
                "WHERE FILM_ID = ? ";

        int result = jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), id);

        if (result == 0) {
            log.debug(String.format("Фильм с id = %d не был найден в базе", id));
            throw new FilmNotFoundException(String.format("Фильм с id = %d не найден в базе", id));
        }

        return film;
    }

    @Override
    public List<Film> getAll() {
        log.debug("Отправляем запрос на все фильмы из БД");
        final String sqlQuery = "SELECT *" +
                "FROM FILMS " +
                "INNER JOIN MPA M on M.MPA_ID = FILMS.MPA_ID ";

        return jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm);
    }

    @Override
    public void addLike(Integer id, Integer idUser) {
        log.debug("Запрос к БД на добавление лайка");
        //Этот запрос для учета информации "кто поставил лайк"
        try {
            final String sqlQuery = "INSERT INTO FILMS_LIKES(FILM_ID, USER_ID) "
                    + "VALUES(?, ?)";

            jdbcTemplate.update(sqlQuery, id, idUser);

        } catch (DuplicateKeyException exp) {
            log.debug(String.format("Лайк фильму с id = %d от пользователя с id = %d уже был поставлен", id, idUser));
            throw new DuplicateLikeException(String.format("Лайк фильму с id = %d от пользователя с id = %d уже был поставлен", id, idUser));
        }

        //Будем учитывать количество лайков сразу в таблице с фильмами
        final String likeQuery = "UPDATE FILMS SET LIKES = LIKES + 1 " +
                "WHERE FILM_ID = ?";

        jdbcTemplate.update(likeQuery, id);
    }

    @Override
    public void removeLike(Integer id, Integer idUser) {
        log.debug("Запрос к БД на удаление лайка");
        final String sqlQuery = "DELETE FROM FILMS_LIKES " +
                "WHERE FILM_ID = ? AND USER_ID = ?";

        jdbcTemplate.update(sqlQuery, id, idUser);

        final String likeQuery = "UPDATE FILMS SET LIKES = LIKES - 1 " +
                "WHERE FILM_ID = ?";

        jdbcTemplate.update(likeQuery, id);
    }

    @Override
    public List<Film> getPopularFilm(Integer count, Integer genreId, Integer year) {
        log.debug("Запрос к БД на топ популярных фильмов");
        //При одинаковом количестве лайков выдаем в порядке ASC id
        String genreFilter = "";
        String yearFilter = "";
        if (genreId != null){
            genreFilter = "JOIN FILMS_GENRE FG on FILMS.FILM_ID = FG.FILM_ID " +
                    "WHERE FG.GENRE_ID = " + genreId + " ";
        }
        if (year != null){
            if (genreId != null){
                yearFilter = "AND EXTRACT(YEAR FROM FILMS.RELEASE_DATE) = " + year + " ";
            } else{
                yearFilter = "WHERE EXTRACT(YEAR FROM FILMS.RELEASE_DATE) = " + year + " ";
            }
        }

        final String sqlQuery = "SELECT* " +
                "FROM FILMS " +
                "INNER JOIN MPA M on M.MPA_ID = FILMS.MPA_ID " +
                genreFilter +
                yearFilter +
                "ORDER BY LIKES DESC, FILMS.FILM_ID " +
                "LIMIT ?";

        return jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm, count);
    }

    @Override
    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        log.debug("Запрос к БД на фильмы конкретного режиссёра");
        String sortingCriteria = "";
        if (sortBy.equals("year")) {
            sortingCriteria = "ORDER BY RELEASE_DATE, FILMS.FILM_ID";
        } else if (sortBy.equals("likes")) {
            sortingCriteria = "ORDER BY LIKES DESC, FILMS.FILM_ID";
        }
        //При одинаковом количестве лайков или равндом годе выдаем в порядке ASC id
        final String sqlQuery = "SELECT * FROM FILMS " +
                "INNER JOIN MPA M on M.MPA_ID = FILMS.MPA_ID " +
                "JOIN FILMS_DIRECTORS FD on FD.FILM_ID = FILMS.FILM_ID " +
                "WHERE FD.DIRECTOR_ID = ? " +
                sortingCriteria;
        return jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm, directorId);
    }

    private static Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        return new Film(
                rs.getInt("FILM_ID"),
                rs.getString("FILM_NAME"),
                rs.getString("FILM_DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")),
                new LinkedHashSet<>(),
                new LinkedHashSet<>()
        );
    }

    private static Film makeSimpleFilm(ResultSet rs, int rowNum) throws SQLException {
        return new Film(
                rs.getInt("FILM_ID"),
                rs.getString("FILM_NAME"),
                rs.getString("FILM_DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                new Mpa(),
                new LinkedHashSet<>(),
                new LinkedHashSet<>()
        );
    }

    @Override
    public void isContains(Integer id) {
        // Используем часть запроса метода get(Integer id).
        //Этого хватает для проверки наличия фильма и не надо загружать жанры и Mpa
        final String simpleQuery = "SELECT *" +
                "FROM FILMS " +
                "WHERE FILM_ID = ? ";

        final List<Film> films = jdbcTemplate.query(simpleQuery, FilmDbStorage::makeSimpleFilm, id);

        if (films.size() == 0) {
            log.debug(String.format("Фильм с id = %d не был найден в базе", id));
            throw new FilmNotFoundException(String.format("Фильм с id = %d не найден в базе", id));
        }
    }

    @Override
    public List<Film> getUserRecommendations(Integer id) {
        log.debug(String.format("Отправляем запрос на рекомендации для пользователя с id = %d из БД", id));
        final String sqlQuery = "SELECT * " +
                "FROM FILMS " +
                "INNER JOIN MPA M on M.MPA_ID = FILMS.MPA_ID " +
                "WHERE FILM_ID IN " +
                "( SELECT FILM_ID " +
                "FROM FILMS_LIKES " +
                "EXCEPT " +
                "SELECT FILM_ID " +
                "FROM FILMS_LIKES " +
                "WHERE USER_ID = ? )";

        return jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm, id);
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        log.debug("Запрос к БД на список общих фильмов у двух пользователей");
        final String sqlQuery = "SELECT * " +
                "FROM FILMS " +
                "INNER JOIN MPA M on M.MPA_ID = FILMS.MPA_ID " +
                "WHERE FILM_ID IN ( " +
                "SELECT FILM_ID " +
                "FROM FILMS_LIKES " +
                "WHERE USER_ID = ?) " +
                "AND FILM_ID IN ( " +
                "SELECT FILM_ID " +
                "FROM FILMS_LIKES " +
                "WHERE USER_ID = ?)" +
                "ORDER BY LIKES DESC, FILM_ID";
        return jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm, userId, friendId);
    }
}
