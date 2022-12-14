package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final FilmService filmService;

    @GetMapping
    public List<User> getUsers() {
        //Вызов функции через создание переменной для логирования данных
        List<User> saveUsers = userService.getUsers();
        log.debug("Список всех пользователей был выдан");
        return saveUsers;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Integer id) {
        User user = userService.getUser(id);
        log.debug(String.format("Пользователь с id = %d был выдан", id));
        return user;
    }

    @PostMapping
    public User addUser(@RequestBody @Valid User user) {
        User saveUser = userService.addUser(user);
        log.debug(String.format("Новый пользователь был добавлен. Выданный id = %d", saveUser.getId()));
        return saveUser;
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable Integer id) {
        userService.removeUser(id);
        log.debug(String.format("Пользователь с id = %d удален", id));
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user) {
        User saveUser = userService.updateUser(user);
        log.debug(String.format("Пользователь с id = %d был обновлен", saveUser.getId()));
        return saveUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.addFriend(id, friendId);
        log.debug(String.format("Пользователю с id = %d был добавлен друг с id = %d", id, friendId));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.removeFriend(id, friendId);
        log.debug(String.format("У пользователя с id = %d был удален друг с id = %d", id, friendId));
    }

    @GetMapping("{id}/friends")
    public List<User> getFriends(@PathVariable Integer id) {
        List<User> friends = userService.getFriends(id);
        log.debug(String.format("Пользователю с id = %d был выдан список друзей", id));
        return friends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        List<User> common = userService.getCommonFriends(id, otherId);
        log.debug(String.format("Список общих друзей id  = %d c otherId = %d был выдан", id, otherId));
        return common;
    }

    @GetMapping("{id}/recommendations")
    public List<Film> getUserRecommendations(@PathVariable Integer id) {
        log.info(String.format("Запрос рекомендаций для пользователя id = %d", id));
        return filmService.getUserRecommendations(id);
    }

    @GetMapping("{id}/feed")
    public List<Event> getUserFeed(@PathVariable Integer id) {
        log.info(String.format("Запрос ленты новостей для пользователя id = %d", id));
        return userService.getUserFeed(id);
    }
}
