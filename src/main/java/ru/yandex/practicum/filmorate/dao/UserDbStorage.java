package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.CountOfResultNotExpectedException;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User add(User user) {
        log.debug("Запрос к БД на добавление пользователя");

        String sqlQuery = "INSERT INTO USERS(USER_EMAIL, USER_NAME, USER_LOGIN, BIRTHDAY) "
                + "VALUES(?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
                    PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"USER_ID"});
                    stmt.setString(1, user.getEmail());
                    stmt.setString(2, user.getName());
                    stmt.setString(3, user.getLogin());
                    stmt.setDate(4, Date.valueOf(user.getBirthday()));
                    return stmt;
                }, keyHolder
        );

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public void remove(Integer id) {
        //Прежде чем удалить юзера, необходимо удалить его лайки и дизлайки на отзывы/фильмы
        //Записи в таблицах удалятся самостоятельно
        //Но необходим перерасчет значений полей likes/useful в фильмы/отзывы

        log.debug("Запрос к БД на обновление USEFUL для отзывов, которые лайкнул удаляемый пользователь");
        final String sqlLikeQuery = "UPDATE REVIEWS SET USEFUL = REVIEWS.USEFUL - 1 " +
                "WHERE REVIEW_ID IN " +
                "(SELECT REVIEW_ID " +
                "FROM REVIEWS_MARK " +
                "WHERE USER_ID = ? AND MARK = ?)";

        jdbcTemplate.update(sqlLikeQuery, id, 1);

        log.debug("Запрос к БД на обновление USEFUL для отзывов, которые дизлайкнул удаляемый пользователь");
        final String sqlDislikeQuery = "UPDATE REVIEWS SET USEFUL = REVIEWS.USEFUL + 1 " +
                "WHERE REVIEW_ID IN " +
                "(SELECT REVIEW_ID " +
                "FROM REVIEWS_MARK " +
                "WHERE USER_ID = ? AND MARK = ?)";

        jdbcTemplate.update(sqlDislikeQuery, id, -1);

        log.debug("Запрос к БД на обновление LIKES для фильмов, которые лайкнул удаляемый пользователь");
        final String sqlFilmLikeQuery = "UPDATE FILMS SET LIKES = LIKES - 1 " +
                "WHERE FILM_ID IN " +
                "(SELECT FILM_ID " +
                "FROM FILMS_LIKES " +
                "WHERE USER_ID = ?)";

        jdbcTemplate.update(sqlFilmLikeQuery, id);

        log.debug("Запрос к БД на удаление пользователя");
        final String sqlQuery = "DELETE FROM USERS " +
                "WHERE USER_ID = ?";

        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public User update(User user) {
        log.debug("Запрос к БД на обновление пользователя");
        int id = user.getId();

        final String sqlQuery = "UPDATE USERS SET " +
                "USER_EMAIL = ?, USER_NAME = ?, USER_LOGIN = ?, BIRTHDAY = ? " +
                "WHERE USER_ID = ? ";

        int result = jdbcTemplate.update(sqlQuery, user.getEmail(), user.getName(),
                user.getLogin(), user.getBirthday(), id);

        if (result == 0) {
            throw new EntityNotFoundException(String.format("Фильм с id = %d не найден в базе", id));
        }
        return user;
    }

    @Override
    public User get(Integer id) {
        log.debug("Запрос к БД на выдачу пользователя");

        final String sqlQuery = "SELECT *" +
                "FROM USERS " +
                "WHERE USER_ID = ? ";

        final List<User> users = jdbcTemplate.query(sqlQuery, UserDbStorage::makeUser, id);

        if (users.size() == 0) {
            throw new EntityNotFoundException(String.format("Пользователь с id = %d не найден в базе", id));
        }

        if (users.size() != 1) {
            throw new CountOfResultNotExpectedException("Количество полученных юзеров не совпадает с ожидаемым");
        }

        return users.get(0);
    }

    @Override
    public List<User> getAll() {
        log.debug("Отправляем запрос на всех юзеров из БД");

        final String sqlQuery = "SELECT* FROM USERS";

        return jdbcTemplate.query(sqlQuery, UserDbStorage::makeUser);
    }

    @Override
    public void addFriend(Integer id, Integer idFriend) {
        log.debug("Запрос к БД на добавление в друзья");

        final String sqlQuery = "INSERT INTO FRIENDS(USER_ID, FRIEND_ID) "
                + "VALUES(?, ?)";

        jdbcTemplate.update(sqlQuery, id, idFriend);
    }

    @Override
    public void removeFriend(Integer id, Integer idFriend) {
        log.debug("Запрос к БД на удаление друга");

        final String sqlQuery = "DELETE FROM FRIENDS " +
                "WHERE USER_ID = ? AND FRIEND_ID = ?";

        jdbcTemplate.update(sqlQuery, id, idFriend);
    }

    @Override
    public List<User> getFriends(Integer id) {
        log.debug("Запрос к БД на выдачу друзей");

        final String sqlQuery = "SELECT* FROM USERS U " +
                "INNER JOIN FRIENDS F on U.USER_ID = F.FRIEND_ID " +
                "WHERE F.USER_ID = ?";

        return jdbcTemplate.query(sqlQuery, UserDbStorage::makeUser, id);
    }

    @Override
    public List<User> getCommonFriend(Integer id, Integer idOther) {
        log.debug("Запрос к БД на выдачу общих друзей");

        final String sqlQuery = "SELECT* FROM USERS U " +
                "INNER JOIN FRIENDS F1 on U.USER_ID = F1.FRIEND_ID " +
                "INNER JOIN FRIENDS F2 on U.USER_ID = F2.FRIEND_ID " +
                "WHERE F1.USER_ID = ?" +
                "AND F2.USER_ID = ?";

        return jdbcTemplate.query(sqlQuery, UserDbStorage::makeUser, id, idOther);
    }

    private static User makeUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getInt("USER_ID"),
                rs.getString("USER_EMAIL"),
                rs.getString("USER_NAME"),
                rs.getString("USER_LOGIN"),
                rs.getDate("BIRTHDAY").toLocalDate()
        );
    }

    @Override
    public void isContains(Integer id) {
        // Если такого юзера нет, вылетит ошибка об отсутствии в базе
        this.get(id);
    }
}
