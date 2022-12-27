package ru.yandex.practicum.filmorate.dao.UserDbStorageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;


    @BeforeEach
    public void addUser() {
        userDbStorage.add(createUser("mail@mail.ru", "Nick Name", "name", "1990-08-20"));
        userDbStorage.add(createUser("yandex@yandex.ru", "Mr Bin", "Bin", "1991-11-23"));
        userDbStorage.add(createUser("rim@mail.ru", "Rim", "Rimus", "1992-07-21"));
    }

    @AfterEach
    public void clearDB() {
        System.out.println("Удаляем данные");
        String sqlQuery = "DROP TABLE FRIENDS ";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "DROP TABLE FILMS_LIKES";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "DROP TABLE EVENTS";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "DROP TABLE REVIEWS_MARK";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "DROP TABLE REVIEWS";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "DROP TABLE USERS ";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "create table IF NOT EXISTS USERS (" +
                "    USER_ID    INTEGER AUTO_INCREMENT," +
                "    USER_EMAIL VARCHAR not null," +
                "    USER_NAME  VARCHAR not null," +
                "    USER_LOGIN VARCHAR not null," +
                "    BIRTHDAY   DATE    not null," +
                "    constraint USERS_PK primary key (USER_ID));" ;

        jdbcTemplate.update(sqlQuery);

        sqlQuery = "create table IF NOT EXISTS REVIEWS(" +
                "    REVIEW_ID   INTEGER AUTO_INCREMENT," +
                "    CONTENT     VARCHAR not null," +
                "    IS_POSITIVE BOOLEAN not null," +
                "    USER_ID     INTEGER not null," +
                "    FILM_ID     INTEGER not null," +
                "    USEFUL      INTEGER DEFAULT 0," +
                "    constraint REVIEWS_PK" +
                "        primary key (REVIEW_ID)," +
                "    constraint REVIEWS_USERS_fk" +
                "        foreign key (USER_ID) references USERS ON DELETE CASCADE," +
                "    constraint REVIEWS_FILMS_fk" +
                "        foreign key (FILM_ID) references FILMS ON DELETE CASCADE" +
                ");";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "create table IF NOT EXISTS FILMS_LIKES( " +
                "FILM_ID INTEGER not null," +
                "USER_ID INTEGER not null," +
                "constraint uq_likes UNIQUE (FILM_ID, USER_ID)," +
        "constraint FILMS_LIKES_fk foreign key (FILM_ID) references FILMS ON DELETE CASCADE," +
        "constraint FILMS_LIKES_USER_fk foreign key (USER_ID) references USERS ON DELETE CASCADE);";

        jdbcTemplate.update(sqlQuery);

        sqlQuery = "create table IF NOT EXISTS FRIENDS( " +
                "USER_ID   INTEGER not null," +
                "FRIEND_ID INTEGER not null," +
                "constraint USER_FRIENDS_fk foreign key (USER_ID) references USERS ON DELETE CASCADE," +
                "constraint FRIEND_USER_fk  foreign key (FRIEND_ID) references USERS (USER_ID) ON DELETE CASCADE);";

        jdbcTemplate.update(sqlQuery);

        sqlQuery = "create table IF NOT EXISTS REVIEWS_MARK(" +
                "    MARK_ID   INTEGER AUTO_INCREMENT," +
                "    REVIEW_ID INTEGER not null," +
                "    USER_ID   INTEGER not null," +
                "    MARK      INT2    not null," +
                "    constraint uq_marks UNIQUE (REVIEW_ID, USER_ID)," +
                "    constraint REVIEWS_MARK_PK" +
                "        primary key (MARK_ID)," +
                "    constraint REVIEWS_MARK_USERS_fk" +
                "        foreign key (USER_ID) references USERS ON DELETE CASCADE," +
                "    constraint REVIEWS_MARK_REVIEWS_fk" +
                "        foreign key (REVIEW_ID) references REVIEWS ON DELETE CASCADE);";
        jdbcTemplate.update(sqlQuery);

        sqlQuery = "create table IF NOT EXISTS EVENTS(" +
                "EVENT_ID INTEGER AUTO_INCREMENT," +
                "    TIME_EVENT TIMESTAMP DEFAULT CURRENT_TIMESTAMP()," +
                "    USER_ID INTEGER NOT NULL," +
                "    EVENT_TYPE VARCHAR NOT NULL," +
                "    OPERATION VARCHAR NOT NULL," +
                "    ENTITY_ID INTEGER NOT NULL," +
                "    constraint EVENTS_PK" +
                "        primary key (EVENT_ID)," +
                "    constraint EVENTS_USERS_fk" +
                "        foreign key (USER_ID) references USERS ON DELETE CASCADE);";
        jdbcTemplate.update(sqlQuery);
    }

    @Test
    public void getUserById() {
        User saveUser1 = userDbStorage.get(1);

        assertEquals(1, saveUser1.getId());
        assertEquals("mail@mail.ru", saveUser1.getEmail());
        assertEquals("Nick Name", saveUser1.getLogin());
        assertEquals("name", saveUser1.getName());
        assertEquals(LocalDate.parse("1990-08-20"), saveUser1.getBirthday());
    }

    @Test
    public void updateUserById() {
        User saveUser1 = userDbStorage.get(1);
        saveUser1.setName("Nick");
        userDbStorage.update(saveUser1);
        User updateUser1 = userDbStorage.get(1);

        assertEquals(1, updateUser1.getId());
        assertEquals("mail@mail.ru", updateUser1.getEmail());
        assertEquals("Nick Name", updateUser1.getLogin());
        assertEquals("Nick", updateUser1.getName());
        assertEquals(LocalDate.parse("1990-08-20"), updateUser1.getBirthday());
    }

    @Test
    public void removeUserById() {
        //Убеждаемся, что в базе есть юзер, которого собираемся удалять
        User saveUser1 = userDbStorage.get(1);

        assertEquals(1, saveUser1.getId());
        assertEquals("mail@mail.ru", saveUser1.getEmail());
        assertEquals("Nick Name", saveUser1.getLogin());
        assertEquals("name", saveUser1.getName());
        assertEquals(LocalDate.parse("1990-08-20"), saveUser1.getBirthday());

        userDbStorage.remove(1);

        final UserNotFoundException exp = assertThrows(UserNotFoundException.class,
                () -> userDbStorage.get(1));
        assertEquals("Пользователь с id = 1 не найден в базе", exp.getMessage());
    }

    @Test
    public void getAllUsers() {
        List<User> users = userDbStorage.getAll();
        User user1 = users.get(0);
        User user2 = users.get(1);
        User user3 = users.get(2);

        assertEquals(1, user1.getId());
        assertEquals("mail@mail.ru", user1.getEmail());
        assertEquals("Nick Name", user1.getLogin());
        assertEquals("name", user1.getName());
        assertEquals(LocalDate.parse("1990-08-20"), user1.getBirthday());

        assertEquals(2, user2.getId());
        assertEquals("yandex@yandex.ru", user2.getEmail());
        assertEquals("Mr Bin", user2.getLogin());
        assertEquals("Bin", user2.getName());
        assertEquals(LocalDate.parse("1991-11-23"), user2.getBirthday());

        assertEquals(3, user3.getId());
        assertEquals("rim@mail.ru", user3.getEmail());
        assertEquals("Rim", user3.getLogin());
        assertEquals("Rimus", user3.getName());
        assertEquals(LocalDate.parse("1992-07-21"), user3.getBirthday());

    }

    @Test
    public void operationWithFriend() {
        List<User> friends = userDbStorage.getFriends(1);
        assertEquals(0, friends.size());

        //Проверка добавления в друзья
        userDbStorage.addFriend(1, 2);
        friends = userDbStorage.getFriends(1);
        assertEquals(1, friends.size());

        User friend = friends.get(0);
        assertEquals(2, friend.getId());
        assertEquals("yandex@yandex.ru", friend.getEmail());
        assertEquals("Mr Bin", friend.getLogin());
        assertEquals("Bin", friend.getName());
        assertEquals(LocalDate.parse("1991-11-23"), friend.getBirthday());

        //Проверка удаления из друзей
        userDbStorage.removeFriend(1, 2);
        friends = userDbStorage.getFriends(1);
        assertEquals(0, friends.size());

    }

    @Test
    public void getCommonFriend() {
        List<User> friends = userDbStorage.getCommonFriend(1, 2);
        assertEquals(0, friends.size());

        userDbStorage.addFriend(1, 2);
        userDbStorage.addFriend(1, 3);
        userDbStorage.addFriend(2, 3);

        friends = userDbStorage.getCommonFriend(1, 2);
        assertEquals(1, friends.size());
        User friend = friends.get(0);

        assertEquals(3, friend.getId());
        assertEquals("rim@mail.ru", friend.getEmail());
        assertEquals("Rim", friend.getLogin());
        assertEquals("Rimus", friend.getName());
        assertEquals(LocalDate.parse("1992-07-21"), friend.getBirthday());

    }

    public static User createUser(String email, String login, String name, String birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.parse(birthday));
        return user;
    }

}
