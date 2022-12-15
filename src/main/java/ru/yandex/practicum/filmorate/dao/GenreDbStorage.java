package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public Genre getGenre(Integer id) {
        final String sqlQuery = "SELECT *" +
                "FROM GENRE " +
                "WHERE GENRE_ID = ? ";

        List<Genre> genre = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeGenre, id);

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

        List<Genre> genre = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeGenre);

        if (genre.isEmpty()) {
            log.debug("Genre не были найдены в базе");
            throw new GenreNotFoundException("Genre не были найдены в базе");
        }

        return genre;
    }
}
