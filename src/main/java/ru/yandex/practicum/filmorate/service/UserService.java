package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private Integer countId = 0;

    public User getUser(Integer id) {
        log.debug(String.format("Выдача пользователя c id = %d", id));
        isContainsUser(id);
        return userStorage.get(id);
    }

    public User addUser(User user) {
        log.debug("Добавление пользователя");
        User saveUser = validateName(user);
        Integer id = getId();
        saveUser.setId(id);

        //Если придет пользователь без поля friends - инициализируем пустым списком
        // для искл. ошибки NullPointerException при обращении к этому полю
        if (saveUser.getFriends() == null) {
            saveUser.setFriends(new HashSet<>());
        }

        userStorage.add(id, saveUser);
        return saveUser;
    }

    public User updateUser(User user) {
        Integer id = user.getId();
        log.debug(String.format("Обновление пользователя c id = %d", id));

        isContainsUser(id);
        User saveUser = validateName(user);

        if (saveUser.getFriends() == null) {
            saveUser.setFriends(new HashSet<>());
        }

        userStorage.add(id, saveUser);
        return saveUser;
    }

    public List<User> getUsers() {
        log.debug("Выдача всех пользователей");
        return userStorage.getAll();
    }

    public void addFriend(Integer id, Integer friendId) {
        log.debug(String.format("Добавление в друзья пользователю c id = %d пользователя с id = %d", id, friendId));
        isContainsUser(id);
        isContainsUser(friendId);

        userStorage.get(id).getFriends().add(friendId);
        userStorage.get(friendId).getFriends().add(id);
    }

    public void removeFriend(Integer id, Integer friendId) {
        log.debug(String.format("Удаление из друзей пользователя c id = %d пользователя с id = %d", id, friendId));
        isContainsUser(id);
        isContainsUser(friendId);

        userStorage.get(id).getFriends().remove(friendId);
        userStorage.get(friendId).getFriends().remove(id);
    }

    public List<User> getAllFriends(Integer id) {
        log.debug(String.format("Выдача списка друзей пользователя c id = %d", id));
        isContainsUser(id);

        return userStorage.get(id).getFriends().stream()
                .map(userStorage::get).collect(Collectors.toList());
    }

    public List<User> getCommonFriend(Integer id, Integer otherId) {
        log.debug(String.format("Поиск общих друзей пользователя c id = %d  и пользователя с id = %d", id, otherId));
        isContainsUser(id);
        isContainsUser(otherId);

        Set<Integer> common = new HashSet<>(userStorage.get(id).getFriends());
        common.retainAll(userStorage.get(otherId).getFriends());
        return common.stream().map(userStorage::get).collect(Collectors.toList());
    }

    private User validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return user;
    }

    public void isContainsUser(Integer id) {
        if (!userStorage.isContains(id)) {
            log.debug(String.format("Пользователь с id = %d не был найден в базе", id));
            throw new UserNotFoundException(String.format("Пользователь с id = %d не был найден в базе", id));
        }
    }

    private Integer getId() {
        return ++countId;
    }
}
