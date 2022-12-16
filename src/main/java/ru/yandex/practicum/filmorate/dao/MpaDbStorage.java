package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.CountOfResultNotExpectedException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public Mpa getMpa(Integer id) {
        final String sqlQuery = "SELECT *" +
                "FROM MPA " +
                "WHERE MPA_ID = ? ";

        List<Mpa> mpa = jdbcTemplate.query(sqlQuery, MpaDbStorage::makeMpa, id);

        if (mpa.isEmpty()) {
            log.debug(String.format("MPA с id = %d не был найден в базе", id));
            throw new MpaNotFoundException(String.format("MPA с id = %d не найден в базе", id));
        }

        if (mpa.size() != 1) {
            throw new CountOfResultNotExpectedException("Количество полученных MPA не совпадает с ожидаемым");
        }

        return mpa.get(0);
    }

    public List<Mpa> getAllMpa() {
        final String sqlQuery = "SELECT *" +
                "FROM MPA ";

        List<Mpa> mpa = jdbcTemplate.query(sqlQuery, MpaDbStorage::makeMpa);

        if (mpa.isEmpty()) {
            log.debug("MPA не были найдены в базе");
            throw new MpaNotFoundException("MPA не были найдены в базе");
        }

        return mpa;
    }

    private static Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("MPA_ID"),
                rs.getString("MPA_NAME"));
    }

}
