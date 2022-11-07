package ru.yandex.practicum.filmorate.controller;

import lombok.Generated;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserUpdateException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int count;

    @GetMapping
    public List<User> getUsers() {
        log.debug("Выданы все пользователи");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User addUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        validateUser(bindingResult);

        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        int id = getId();
        user.setId(id);
        users.put(id, user);
        log.debug("Новый пользователь добавлен. Выданный id = " + id);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        validateUser(bindingResult);
        int id = user.getId();

        if (!users.containsKey(id)) {
            log.debug("Пользователь не может быть обновлен, так как отсутствует в базе данных");
            throw new UserUpdateException();
        }

        users.put(id, user);
        log.debug("Пользователь с id = " + id + " был обновлен");
        return user;
    }

    private int getId() {
        return ++count;
    }

    //Для подробной записи ошибок в лог
    private void validateUser(BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("email")) {
            log.debug("Ошибка валидации пользователя. Неверный email");
            throw new UserValidationException();
        }

        if (bindingResult.hasFieldErrors("birthday")) {
            log.debug("Ошибка валидации пользователя. Дата рождения не может быть в будущем");
            throw new UserValidationException();
        }

        if (bindingResult.hasFieldErrors("login")) {
            log.debug("Ошибка валидации пользователя. Логин не может быть пустым");
            throw new UserValidationException();
        }
    }
}
