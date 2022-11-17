package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    @GetMapping
    public List<User> getUsers() {
        log.debug("Выданы все пользователи");
        return service.getUsers();
    }

    @PostMapping
    public User addUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        validateUser(bindingResult);
        User saveUser = service.addUser(user);
        log.debug("Новый пользователь добавлен. Выданный id = " + saveUser.getId());
        return saveUser;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        validateUser(bindingResult);
        User saveUser = service.updateUser(user);
        log.debug("Пользователь с id = " + saveUser.getId() + " был обновлен");
        return saveUser;
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
            log.debug("Ошибка валидации пользователя. Логин не может быть пустым и содержать пробелы");
            throw new UserValidationException();
        }
    }
}
