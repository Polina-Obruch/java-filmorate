package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.Set;

public class UsersValidatorTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Проверка невалидности login")
    void addUserWithFailLogin() {
        User user = createUser("asd@Yandex.ru", " ", "Asd", "1995-10-25");
        User user1 = createUser("asd1@Yandex.ru", null, "Asd", "1995-10-25");
        Assertions.assertAll(
                () -> Assertions.assertTrue(requestUser(user, "Логин не может быть пустым")),
                () -> Assertions.assertTrue(requestUser(user1, "Логин не может быть пустым"))
        );
    }

    @Test
    @DisplayName("Проверка невалидности email")
    void addUserWithFailEmail() {
        User user = createUser("asdYandex.ru", "aaa", "Asd", "1995-10-25");
        User user1 = createUser(" ", "aaa", "Asd", "1995-10-25");
        User user2 = createUser(null, "aaa", "Asd", "1995-10-25");
        User user3 = createUser("asdYandex@", "aaa", "Asd", "1995-10-25");

        Assertions.assertAll(
                () -> Assertions.assertTrue(requestUser(user, "Email введен с ошибкой")),
                () -> Assertions.assertTrue(requestUser(user1, "Email не может быть пустым")),
                () -> Assertions.assertTrue(requestUser(user2, "Email не может быть пустым")),
                () -> Assertions.assertTrue(requestUser(user3, "Email введен с ошибкой"))
        );
    }

    @Test
    @DisplayName("Проверка невалидности birthday")
    void addUserWithFailBirthday() {
        User user = createUser("asd@Yandex.ru", " ", "Asd", "2046-10-25");
        Assertions.assertTrue(requestUser(user, "Дата рождения не может быть в будущем"));
    }

    private User createUser(String email, String login, String name, String birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.parse(birthday));
        return user;
    }

    private boolean requestUser(User user, @NotEmpty String message) {
        Set<ConstraintViolation<User>> errors = VALIDATOR.validate(user);
        return errors.stream().map(ConstraintViolation::getMessage).anyMatch(message::equals);
    }
}
