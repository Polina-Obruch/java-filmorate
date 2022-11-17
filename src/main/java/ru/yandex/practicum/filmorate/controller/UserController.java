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

    @GetMapping("/{id}")
    public User getUser(@PathVariable Integer id) {
        User user = service.getUser(id);
        log.debug(String.format("Выдан пользователь с id = %d", id));
        return user;
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

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        service.addFriend(id, friendId);
        log.debug(String.format("Пользователю с id = %d был добавлен друг с id = %d", id, friendId));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        service.removeFriend(id, friendId);
        log.debug(String.format("У пользователя с id = %d был удален друг с id = %d", id, friendId));
    }

    @GetMapping("{id}/friends")
    public List<User> getAllFriend(@PathVariable Integer id) {
        return service.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriend(@PathVariable Integer id, @PathVariable Integer otherId) {
        return service.getCommonFriend(id, otherId);
    }


    //Для подробной записи ошибок в лог
    private void validateUser(BindingResult bindingResult) {
        String errorMessage;

        if (bindingResult.hasFieldErrors("email")) {
            errorMessage = "Ошибка валидации данных пользователя. Неверный email";
            log.error(errorMessage);
            throw new UserValidationException(errorMessage);
        }

        if (bindingResult.hasFieldErrors("birthday")) {
            errorMessage = "Ошибка валидации данных пользователя. Дата рождения не может быть в будущем";
            log.error(errorMessage);
            throw new UserValidationException(errorMessage);
        }

        if (bindingResult.hasFieldErrors("login")) {
            errorMessage = "Ошибка валидации данных пользователя. Логин не может быть пустым и содержать пробелы";
            log.error(errorMessage);
            throw new UserValidationException(errorMessage);
        }
    }
}
