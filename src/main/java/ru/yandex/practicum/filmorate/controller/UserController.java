package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserUpdateException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service = new UserService();
    private int count;

    @GetMapping
    public List<User> getUsers() {
        log.debug("Выданы все пользователи");
        return service.getUsers();
    }

    @PostMapping
    public User addUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        validateUser(bindingResult);
        int id = getId();
        User saveUser = service.addUser(id, user);
        log.debug("Новый пользователь добавлен. Выданный id = " + id);
        return saveUser;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        validateUser(bindingResult);
        int id = user.getId();

        //Не отправляем данные сервису, пока не убедимся в необходимости этого
        if (!service.isContains(id)) {
            log.debug("Пользователь не может быть обновлен, так как отсутствует в базе данных");
            throw new UserUpdateException();
        }

        User saveUser = service.updateUser(id, user);
        log.debug("Пользователь с id = " + id + " был обновлен");
        return saveUser;
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
            log.debug("Ошибка валидации пользователя. Логин не может быть пустым и содержать пробелы");
            throw new UserValidationException();
        }
    }
}
