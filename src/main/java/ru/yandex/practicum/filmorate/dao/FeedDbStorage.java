package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class FeedDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Event> getFeed(Integer id) {
        log.debug("Запрос к БД на выдачу ленты новостей");
        final String sqlQuery = "SELECT* FROM EVENTS " +
                "WHERE USER_ID = ?";

        return jdbcTemplate.query(sqlQuery, FeedDbStorage::makeEvent, id);
    }

    public void removeFriend(Integer id, Integer idFriend) {
        log.debug("Запрос к БД на новость об удалении друга");
        final String sqlFeed = "INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) "
                + "VALUES(?, ?, ?, ?)";

        jdbcTemplate.update(sqlFeed, id, String.valueOf(EventType.FRIEND),
                String.valueOf(EventOperation.REMOVE), idFriend);
    }

    public void addFriend(Integer id, Integer idFriend) {
        log.debug("Запрос к БД на новость о добавлении друга");
        final String sqlFeed = "INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) "
                + "VALUES(?, ?, ?, ?)";

        jdbcTemplate.update(sqlFeed, id, String.valueOf(EventType.FRIEND),
                String.valueOf(EventOperation.ADD), idFriend);
    }

    public void addLike(Integer filmId, Integer userId) {
        log.debug("Запрос к БД на новость о добавлении лайка к фильму");
        final String sqlFeed = "INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) "
                + "VALUES(?, ?, ?, ?)";

        jdbcTemplate.update(sqlFeed, userId, String.valueOf(EventType.LIKE),
                String.valueOf(EventOperation.ADD), filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        log.debug("Запрос к БД на новость об удалении лайка к фильму");
        final String sqlFeed = "INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) "
                + "VALUES(?, ?, ?, ?)";

        jdbcTemplate.update(sqlFeed, userId, String.valueOf(EventType.LIKE),
                String.valueOf(EventOperation.REMOVE), filmId);
    }

    public void addReview(Integer reviewId, Integer userId) {
        log.debug("Запрос к БД на новость о добавлении отзыва");
        final String sqlFeed = "INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) "
                + "VALUES(?, ?, ?, ?)";

        jdbcTemplate.update(sqlFeed, userId, String.valueOf(EventType.REVIEW),
                String.valueOf(EventOperation.ADD), reviewId);
    }

    public void removeReview(Integer reviewId, Integer userId) {
        log.debug("Запрос к БД на новость об удалении отзыва");
        final String sqlFeed = "INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) "
                + "VALUES(?, ?, ?, ?)";

        jdbcTemplate.update(sqlFeed, userId, String.valueOf(EventType.REVIEW),
                String.valueOf(EventOperation.REMOVE), reviewId);
    }

    public void updateReview(Integer reviewId, Integer userId) {
        log.debug("Запрос к БД на новость об обновлении отзыва");
        final String sqlFeed = "INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) "
                + "VALUES(?, ?, ?, ?)";

        jdbcTemplate.update(sqlFeed, userId, String.valueOf(EventType.REVIEW),
                String.valueOf(EventOperation.UPDATE), reviewId);
    }


    private static Event makeEvent(ResultSet rs, int rowNum) throws SQLException {
        return new Event(
                rs.getInt("EVENT_ID"),
                rs.getInt("USER_ID"),
                rs.getInt("ENTITY_ID"),
                EventType.valueOf(rs.getString("EVENT_TYPE")),
                EventOperation.valueOf(rs.getString("OPERATION")),
                (rs.getTimestamp("TIME_EVENT")).getTime()
        );
    }
}
