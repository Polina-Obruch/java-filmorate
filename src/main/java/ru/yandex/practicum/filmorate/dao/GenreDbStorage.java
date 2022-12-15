package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;


    public Genre getGenre(Integer id) {
        final String sqlQuery = "SELECT *" +
                "FROM GENRE " +
                "WHERE GENRE_ID = ? ";

        List<Genre> genre = jdbcTemplate.query(sqlQuery, GenreDbStorage::makeGenre, id);

        if (genre.isEmpty()) {
            log.debug(String.format("Genre с id = %d не был найден в базе", id));
            throw new GenreNotFoundException(String.format("Genre с id = %d не найден в базе", id));
        }

        if (genre.size() != 1) {
            throw new RuntimeException();
        }

        return genre.get(0);
    }


    public List<Genre> getAllGenre() {
        final String sqlQuery = "SELECT *" +
                "FROM GENRE ";


        List<Genre> genre = jdbcTemplate.query(sqlQuery, GenreDbStorage::makeGenre);

        if (genre.isEmpty()) {
            log.debug("Genre не были найдены в базе");
            throw new GenreNotFoundException("Genre не были найдены в базе");
        }

        return genre;
    }


    public void setFilmGenre(Film film) {
        log.debug("Запрос к БД на удаление старых жанров");
        Integer id = film.getId();
        final String sqlQuery = "DELETE FROM FILMS_GENRE " +
                "WHERE FILM_ID = ? ";

        jdbcTemplate.update(sqlQuery, id);
        ArrayList<Genre> genres = new ArrayList<>(film.getGenres());

        // Если жанров нет, то сохранять их не надо
        if (genres.isEmpty()) {
            return;
        }

        log.debug("Запрос к БД на сохранение жанров для фильма");
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

    public Film loadFilmGenre(Film film) {
        log.debug("Запрос к БД на загрузку жанров");
        final String sqlQuery = "SELECT *" +
                "FROM FILMS_GENRE F " +
                "INNER JOIN GENRE G on G.GENRE_ID = F.GENRE_ID " +
                "WHERE FILM_ID = ? ";

        final List<Genre> genres = jdbcTemplate.query(sqlQuery, GenreDbStorage::makeGenre, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
        return film;
    }


    public List<Film> loadFilmsGenre(List<Film> films) {
        log.debug("Запрос к БД на загрузку жанров для нескольких фильмов");
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

    private static Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(
                rs.getInt("GENRE_ID"),
                rs.getString("GENRE_NAME")
        );
    }
}
