package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserUpdateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;


import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private Integer countId;

    @Autowired
    public UserService (UserStorage userStorage) {
        this.userStorage = userStorage;
        this.countId = 0;
    }

    public User addUser(User user) {
        User saveUser = validateName(user);
        Integer id  = getId();
        saveUser.setId(id);
        userStorage.add(id, saveUser);
        return saveUser;
    }

    public User updateUser(User user) {
        User saveUser = validateName(user);
        Integer id  = saveUser.getId();
        if (userStorage.get(id) != null) {
            userStorage.add( id, saveUser);
            return saveUser;
        }

        log.debug("Пользователь не может быть обновлен, так как отсутствует в базе данных");
        throw  new UserUpdateException();
    }

    public List<User> getUsers() {
        return userStorage.getAll();
    }

    private User validateName(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        return user;
    }

    private Integer getId() {
        return ++countId;
    }
}
