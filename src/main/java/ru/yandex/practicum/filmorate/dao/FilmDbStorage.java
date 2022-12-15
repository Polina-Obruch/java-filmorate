package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicateLikeException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {
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
        setFilmGenre(id, film);
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
                "INNER JOIN MPA M on M.MPA_ID = FILMS.MPA_ID " +
                "WHERE FILM_ID = ? ";

        final List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm, id);

        if (films.size() == 0) {
            log.debug(String.format("Фильм с id = %d не был найден в базе", id));
            throw new FilmNotFoundException(String.format("Фильм с id = %d не найден в базе", id));
        }

        if (films.size() != 1) {
            throw new RuntimeException();
        }

        return loadFilmGenre(films.get(0));
    }

    @Override
    public Film update(Film film) {
        log.debug("Запрос к БД на обновление фильма");
        int id = film.getId();

        final String sqlQuery = "UPDATE FILMS SET " +
                "film_id = ?, film_name = ?, film_description = ?, release_date = ?, duration = ?, mpa_id = ?" +
                "WHERE FILM_ID = ? ";

        int result = jdbcTemplate.update(sqlQuery, id, film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), id);

        if (result == 0) {
            log.debug(String.format("Фильм с id = %d не был найден в базе", id));
            throw new FilmNotFoundException(String.format("Фильм с id = %d не найден в базе", id));
        }

        setFilmGenre(id, film);
        return film;
    }

    @Override
    public List<Film> getAll() {
        log.debug("Отправляем запрос на все фильмы из БД");
        final String sqlQuery = "SELECT *" +
                "FROM FILMS " +
                "INNER JOIN MPA M on M.MPA_ID = FILMS.MPA_ID ";

        List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm);

        // Если фильмов в базе нет, то и запрос на жанры не нужен
        if (films.size() == 0) {
            return films;
        }

        films = loadFilmsGenre(films);

        return films;
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
    public List<Film> getPopularFilm(Integer count) {
        log.debug("Запрос к БД на топ популярных фильмов");
        //При одинаковом количестве лайков выдаем в порядке ASC id
        final String sqlQuery = "SELECT* " +
                "FROM FILMS " +
                "INNER JOIN MPA M on M.MPA_ID = FILMS.MPA_ID " +
                "ORDER BY LIKES DESC, FILM_ID ASC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm, count);

        // Если фильмов в базе нет, то и запрос на жанры не нужен
        if (films.size() == 0) {
            return films;
        }

        films = loadFilmsGenre(films);
        return films;
    }

    private void setFilmGenre(Integer id, Film film) {
        log.debug("Удаляем старые жанры");
        final String sqlQuery = "DELETE FROM FILMS_GENRE " +
                "WHERE FILM_ID = ? ";

        jdbcTemplate.update(sqlQuery, id);
        ArrayList<Genre> genres = new ArrayList<>(film.getGenres());

        // Если жанров нет, то сохранять их не надо
        if (genres.isEmpty()) {
            return;
        }

        log.debug("Начинаем сохранять жанры для фильма");
        jdbcTemplate.batchUpdate("INSERT INTO FILMS_GENRE " +
                        "VALUES ( ?, ? )", new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Genre genre = genres.get(i);
                        ps.setInt(1, id);
                        ps.setInt(2, genre.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return genres.size();
                    }
                }
        );
    }

    private Film loadFilmGenre(Film film) {
        log.debug("Загружаем жанры для фильма");
        final String sqlQuery = "SELECT *" +
                "FROM FILMS_GENRE F " +
                "INNER JOIN GENRE G on G.GENRE_ID = F.GENRE_ID " +
                "WHERE FILM_ID = ? ";

        final List<Genre> genres = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeGenre, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
        return film;
    }


    private List<Film> loadFilmsGenre(List<Film> films) {
        log.debug("Загружаем жанры для всех фильмов");
        List<Integer> ids = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Integer, Film> filmMap = films.stream().collect
                (Collectors.toMap(Film::getId, film -> film));

        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        String sqlQuery = "SELECT *" +
                "FROM FILMS_GENRE F " +
                "INNER JOIN GENRE G on G.GENRE_ID = F.GENRE_ID " +
                "WHERE FILM_ID IN (:ids)";

        namedJdbcTemplate.query(sqlQuery, parameters, (rs, rowNum) ->
                filmMap.get(rs.getInt("FILM_ID"))
                        .getGenres()
                        .add(makeGenre(rs, rowNum)));

        return new ArrayList<>(filmMap.values());
    }


    private static Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        return new Film(
                rs.getInt("FILM_ID"),
                rs.getString("FILM_NAME"),
                rs.getString("FILM_DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")),
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
                new LinkedHashSet<>()
        );
    }


    public static Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(
                rs.getInt("GENRE_ID"),
                rs.getString("GENRE_NAME")
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
}
