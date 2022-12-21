package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.CountOfResultNotExpectedException;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director getDirectorById(int id) {
        log.debug("Запрос к БД на выдачу режиссёра");
        final String sqlQuery = "SELECT * " +
                "FROM DIRECTORS " +
                "WHERE DIRECTOR_ID = ?";

        final List<Director> directors = jdbcTemplate.query(sqlQuery, DirectorDbStorage::makeDirector, id);

        if (directors.size() == 0) {
            log.debug(String.format("Режиссёр с id = %d не был найден в базе", id));
            throw new DirectorNotFoundException(String.format("Режиссёр с id = %d не найден в базе", id));
        }

        if (directors.size() != 1) {
            throw new CountOfResultNotExpectedException("Количество полученных режиссёров не совпадает с ожидаемым");
        }

        return directors.get(0);
    }

    @Override
    public List<Director> getAllDirectors() {
        log.debug("Отправляем запрос на всех режиссёров из БД");
        final String sqlQuery = "SELECT * FROM DIRECTORS";

        return jdbcTemplate.query(sqlQuery, DirectorDbStorage::makeDirector);
    }

    @Override
    public Director createDirector(Director director) {
        log.debug("Запрос к БД на добавление режиссёра");
        String sqlQuery = "INSERT INTO DIRECTORS(DIRECTOR_NAME) "
                + "VALUES(?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
                    PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"DIRECTOR_ID"});
                    stmt.setString(1, director.getName());
                    return stmt;
                }, keyHolder
        );

        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        log.debug("Запрос к БД на обновление режиссёра");
        int id = director.getId();

        final String sqlQuery = "UPDATE DIRECTORS SET " +
                "DIRECTOR_NAME = ? " +
                "WHERE DIRECTOR_ID = ? ";

        int result = jdbcTemplate.update(sqlQuery, director.getName(), id);

        if (result == 0) {
            log.debug(String.format("Режиссёр с id = %d не был найден в базе", id));
            throw new DirectorNotFoundException(String.format("Режиссёр с id = %d не найден в базе", id));
        }
        return director;
    }

    @Override
    public void removeDirector(int id){
        log.debug("Запрос к БД на удаление режиссёра");
        final String sqlQuery = "DELETE FROM DIRECTORS " +
                "WHERE DIRECTOR_ID = ?";

        jdbcTemplate.update(sqlQuery, id);
    }

    public void setFilmDirector(Film film) {
        log.debug("Запрос к БД на удаление старых режиссёров");
        Integer id = film.getId();
        final String sqlQuery = "DELETE FROM FILMS_DIRECTORS " +
                "WHERE FILM_ID = ? ";

        jdbcTemplate.update(sqlQuery, id);
        ArrayList<Director> directors = new ArrayList<>(film.getDirectors());

        // Если режиссёров нет, то сохранять их не надо
        if (directors.isEmpty()) {
            return;
        }

        log.debug("Запрос к БД на сохранение режиссёров для фильма");
        jdbcTemplate.batchUpdate("INSERT INTO FILMS_DIRECTORS " +
                        "VALUES ( ?, ? )", new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Director director = directors.get(i);
                        ps.setInt(1, id);
                        ps.setInt(2, director.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return directors.size();
                    }
                }
        );
    }

    public Film loadFilmDirector(Film film) {
        log.debug("Запрос к БД на загрузку режиссёров");
        final String sqlQuery = "SELECT *" +
                "FROM FILMS_DIRECTORS F " +
                "INNER JOIN DIRECTORS D on D.DIRECTOR_ID = F.DIRECTOR_ID " +
                "WHERE FILM_ID = ? ";

        final List<Director> directors = jdbcTemplate.query(sqlQuery, DirectorDbStorage::makeDirector, film.getId());
        film.setDirectors(new LinkedHashSet<>(directors));
        return film;
    }

    public List<Film> loadFilmsDirector(List<Film> films) {
        log.debug("Запрос к БД на загрузку режиссёров для нескольких фильмов");
        List<Integer> ids = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Integer, Film> filmMap = new LinkedHashMap<>();
        for (Film f: films){
            filmMap.put(f.getId(),f);
        }

        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        String sqlQuery = "SELECT * " +
                "FROM FILMS_DIRECTORS F " +
                "INNER JOIN DIRECTORS D on D.DIRECTOR_ID = F.DIRECTOR_ID " +
                "WHERE FILM_ID IN (:ids)";

        namedJdbcTemplate.query(sqlQuery, parameters, (rs, rowNum) ->
                filmMap.get(rs.getInt("FILM_ID"))
                        .getDirectors()
                        .add(makeDirector(rs, rowNum)));

        return new ArrayList<>(filmMap.values());
    }

    @Override
    public void isContains(Integer id) {
        // Используем часть запроса метода get(Integer id).
        //Этого хватает для проверки наличия режиссёра
        final String simpleQuery = "SELECT *" +
                "FROM DIRECTORS " +
                "WHERE DIRECTOR_ID = ? ";

        final List<Director> directors = jdbcTemplate.query(simpleQuery, DirectorDbStorage::makeDirector, id);

        if (directors.size() == 0) {
            log.debug(String.format("Режиссёр с id = %d не был найден в базе", id));
            throw new DirectorNotFoundException(String.format("Режиссёр с id = %d не найден в базе", id));
        }
    }

    private static Director makeDirector(ResultSet rs, int rowNum) throws SQLException {
        return new Director(
                rs.getInt("DIRECTOR_ID"),
                rs.getString("DIRECTOR_NAME")
        );
    }

}
