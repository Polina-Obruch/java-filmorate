package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.CountOfResultNotExpectedException;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
@Repository
public class ReviewDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Review add(Review review) {
        log.debug("Запрос к БД на сохранение отзыва");
        String sqlQuery = "INSERT INTO REVIEWS(CONTENT, IS_POSITIVE, USER_ID, FILM_ID) "
                + "VALUES(?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"review_id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            return stmt;
        }, keyHolder);

        Integer id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        review.setReviewId(id);
        return review;
    }

    public Review update(Review review) {
        log.debug("Запрос к БД на обновление отзыва");
        int id = review.getReviewId();

        final String sqlQuery = "UPDATE REVIEWS SET " +
                "CONTENT = ?, IS_POSITIVE = ?"+
                "WHERE REVIEW_ID = ? ";

        int result = jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), id);

        if (result == 0) {
            log.debug(String.format("Отзыв с id = %d не был найден в базе", id));
            throw new ReviewNotFoundException(String.format("Отзыв с id = %d не найден в базе", id));
        }

        return this.get(id);
    }

    public Review get(Integer id) {
        final String sqlQuery = "SELECT *" +
                "FROM REVIEWS " +
                "WHERE REVIEW_ID = ? ";

        final List<Review> review = jdbcTemplate.query(sqlQuery, ReviewDbStorage::makeReview, id);

        if (review.size() == 0) {
            log.debug(String.format("Отзыв с id = %d не был найден в базе", id));
            throw new ReviewNotFoundException(String.format("Отзыв с id = %d не найден в базе", id));
        }

        if (review.size() != 1) {
            throw new CountOfResultNotExpectedException("Количество полученных отзывов не совпадает с ожидаемым");
        }
        return review.get(0);
    }

    public void remove(Integer id) {
        log.debug("Запрос к БД на удаление отзыва");
        final String sqlQuery = "DELETE FROM REVIEWS " +
                "WHERE REVIEW_ID = ? ";

        jdbcTemplate.update(sqlQuery, id);
    }

    private static Review makeReview(ResultSet rs, int rowNum) throws SQLException {
        return new Review(
                rs.getInt("REVIEW_ID"),
                rs.getString("CONTENT"),
                rs.getBoolean("IS_POSITIVE"),
                rs.getInt("USER_ID"),
                rs.getInt("FILM_ID"),
                rs.getInt("USEFUL")
        );
    }


}
