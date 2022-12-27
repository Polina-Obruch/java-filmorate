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
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
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

    @Override
    public Review update(Review review) {
        log.debug("Запрос к БД на обновление отзыва");
        int id = review.getReviewId();

        final String sqlQuery = "UPDATE REVIEWS SET " +
                "CONTENT = ?, IS_POSITIVE = ?" +
                "WHERE REVIEW_ID = ? ";

        int result = jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), id);

        if (result == 0) {
            log.debug(String.format("Отзыв с id = %d не был найден в базе", id));
            throw new ReviewNotFoundException(String.format("Отзыв с id = %d не найден в базе", id));
        }

        return this.get(id);
    }

    @Override
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

    @Override
    public Integer remove(Integer id) {
        final String sqlId = "SELECT USER_ID " +
                "FROM REVIEWS " +
                "WHERE REVIEW_ID = ? ";

        Integer user_Id = Objects.requireNonNull(jdbcTemplate.queryForObject(sqlId,
                Integer.class, id));

        log.debug("Запрос к БД на удаление отзыва");
        final String sqlQuery = "DELETE FROM REVIEWS " +
                "WHERE REVIEW_ID = ? ";

        jdbcTemplate.update(sqlQuery, id);

        return user_Id;
    }

    @Override
    public List<Review> getAllReviews(Integer count) {
        log.debug("Отправляем запрос на все отзывы из БД");
        final String sqlQuery = "SELECT *" +
                "FROM REVIEWS " +
                "ORDER BY USEFUL DESC, REVIEW_ID " +
                "LIMIT ?";

        return jdbcTemplate.query(sqlQuery, ReviewDbStorage::makeReview, count);
    }

    @Override
    public List<Review> getAllReviewsByFilmId(Integer filmId, Integer count) {
        log.debug("Отправляем запрос на все отзывы из БД для фильма");
        final String sqlQuery = "SELECT *" +
                "FROM REVIEWS " +
                "WHERE FILM_ID = ? " +
                "ORDER BY USEFUL DESC, REVIEW_ID " +
                "LIMIT ?";

        return jdbcTemplate.query(sqlQuery, ReviewDbStorage::makeReview, filmId, count);
    }

    @Override
    public void isReviewContains(Integer id) {
        //Более простой запрос на выдачу отзыва для проверки наличия записи
        final String sqlQuery = "SELECT REVIEW_ID " +
                "FROM REVIEWS " +
                "WHERE REVIEW_ID = ? ";

        List<Review> review = jdbcTemplate.query(sqlQuery, ReviewDbStorage::makeSimpleReview, id);

        if (review.size() == 0) {
            log.debug(String.format("Отзыв с id = %d не был найден в базе", id));
            throw new ReviewNotFoundException(String.format("Отзыв с id = %d не найден в базе", id));
        }

        if (review.size() != 1) {
            throw new CountOfResultNotExpectedException("Количество полученных отзывов не совпадает с ожидаемым");
        }
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        log.debug("Запрос к БД на добавление лайка к отзыву");

        try {
            //Этот запрос для учета информации "кто поставил лайк"
            final String sqlQuery = "INSERT INTO REVIEWS_MARK(REVIEW_ID, USER_ID, MARK) "
                    + "VALUES(?, ?, ?)";

            jdbcTemplate.update(sqlQuery, id, userId, 1);

            //Будем учитывать useful сразу в таблице с отзывов
            final String likeQuery = "UPDATE REVIEWS SET USEFUL = USEFUL + 1 " +
                    "WHERE REVIEW_ID = ?";

            jdbcTemplate.update(likeQuery, id);

        } catch (DuplicateKeyException exp) {
            // Т.е. в таблице уже есть запись про лайк или дизлайк для этого отзыва от этого юзера
            // Проверяем, что он до этого ставил
            // Так как такая ситуация происходит редко, то 2 доп.запроса в БД лучше, чем каждый раз при постановке лайка
            // проходиться по всей таблице и пересчитывать useful

            final String sqlMARK = "SELECT MARK " +
                    "FROM REVIEWS_MARK " +
                    "WHERE REVIEW_ID = ? AND USER_ID = ?";

            Integer mark = Objects.requireNonNull(jdbcTemplate.queryForObject(sqlMARK,
                    Integer.class, id, userId));

            //Если он ставит опять лайк -  так нельзя
            if (mark == 1) {
                log.debug(String.format(
                        "Лайк отзыву с id = %d от пользователя с id = %d уже был поставлен", id, userId));
                throw new DuplicateLikeException(String.format(
                        "Лайк отзыву с id = %d от пользователя с id = %d уже был поставлен", id, userId));
            } else {
                //Если хочет поменять дизлайк на лайк - обновляем данные
                final String sqlUpdateMark = "UPDATE REVIEWS_MARK SET " +
                        "MARK = ?" +
                        "WHERE REVIEW_ID = ? AND USER_ID = ?";

                jdbcTemplate.update(sqlUpdateMark, 1, id, userId);

                //Обновляем useful сразу в таблице с отзывов
                //Добавляем 2-ку, так как убрали один дизлайк + добавили лайк
                final String usefulQuery = "UPDATE REVIEWS SET USEFUL = USEFUL + 2 " +
                        "WHERE REVIEW_ID = ?";

                jdbcTemplate.update(usefulQuery, id);
            }
        }
    }

    @Override
    public void addDislike(Integer id, Integer userId) {
        log.debug("Запрос к БД на добавление дизлайка к отзыву");

        try {
            //Этот запрос для учета информации "кто поставил дизлайк"
            final String sqlQuery = "INSERT INTO REVIEWS_MARK(REVIEW_ID, USER_ID, MARK) "
                    + "VALUES(?, ?, ?)";

            jdbcTemplate.update(sqlQuery, id, userId, -1);

            //Будем учитывать useful сразу в таблице с отзывов
            final String disLikeQuery = "UPDATE REVIEWS SET USEFUL = USEFUL - 1 " +
                    "WHERE REVIEW_ID = ?";

            jdbcTemplate.update(disLikeQuery, id);

        } catch (DuplicateKeyException exp) {
            // Т.е. в таблице уже есть запись про лайк или дизлайк для этого отзыва от этого юзера
            // Проверяем, что он до этого ставил

            final String sqlMARK = "SELECT MARK " +
                    "FROM REVIEWS_MARK " +
                    "WHERE REVIEW_ID = ? AND USER_ID = ?";

            Integer mark = Objects.requireNonNull(jdbcTemplate.queryForObject(sqlMARK,
                    Integer.class, id, userId));

            //Если он ставит опять дизлайк -  так нельзя
            if (mark == -1) {
                log.debug(String.format(
                        "Дизлайк отзыву с id = %d от пользователя с id = %d уже был поставлен", id, userId));
                throw new DuplicateLikeException(String.format(
                        "Дизлайк отзыву с id = %d от пользователя с id = %d уже был поставлен", id, userId));
            } else {
                //Если хочет поменять лайк на дизлайк - обновляем данные
                final String sqlUpdateMark = "UPDATE REVIEWS_MARK SET " +
                        "MARK = ?" +
                        "WHERE REVIEW_ID = ? AND USER_ID = ?";

                jdbcTemplate.update(sqlUpdateMark, -1, id, userId);

                //Обновляем useful сразу в таблице с отзывов
                //Отнимаем 2-ку, так как убрали один лайк + добавили дизлайк
                final String disLikeQuery = "UPDATE REVIEWS SET USEFUL = USEFUL - 2 " +
                        "WHERE REVIEW_ID = ?";

                jdbcTemplate.update(disLikeQuery, id);
            }
        }
    }

    @Override
    public void removeLike(Integer id, Integer userId) {
        log.debug("Запрос к БД на удаление лайка у отзыва");
        final String sqlQuery = "DELETE FROM REVIEWS_MARK " +
                "WHERE REVIEW_ID = ? AND USER_ID = ? AND MARK = ?";

        jdbcTemplate.update(sqlQuery, id, userId, 1);

        final String usefulQuery = "UPDATE REVIEWS SET USEFUL = USEFUL - 1 " +
                "WHERE REVIEW_ID = ?";

        jdbcTemplate.update(usefulQuery, id);
    }

    @Override
    public void removeDislike(Integer id, Integer userId) {
        log.debug("Запрос к БД на удаление дизлайка у отзыва");
        final String sqlQuery = "DELETE FROM REVIEWS_MARK " +
                "WHERE REVIEW_ID = ? AND USER_ID = ? AND MARK = ?";

        jdbcTemplate.update(sqlQuery, id, userId, -1);

        final String usefulQuery = "UPDATE REVIEWS SET USEFUL = USEFUL + 1 " +
                "WHERE REVIEW_ID = ?";

        jdbcTemplate.update(usefulQuery, id);
    }

    private static Review makeSimpleReview(ResultSet rs, int rowNum) throws SQLException {
        return new Review(
                rs.getInt("REVIEW_ID")
        );
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
