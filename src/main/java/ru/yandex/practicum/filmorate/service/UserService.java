package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User getUser(Integer id) {
        log.debug(String.format("Выдача пользователя c id = %d", id));
        return userStorage.get(id);
    }

    public User addUser(User user) {
        log.debug("Добавление пользователя");
        User saveUser = validateName(user);
        return userStorage.add(saveUser);
    }

    public User updateUser(User user) {
        Integer id = user.getId();
        log.debug(String.format("Обновление пользователя c id = %d", id));
        return userStorage.update(validateName(user));
    }

    public void removeUser(Integer id) {
        log.debug(String.format("Удаление пользователя c id = %d", id));
        userStorage.remove(id);
    }

    public List<User> getUsers() {
        log.debug("Выдача всех пользователей");
        return userStorage.getAll();
    }

    public void addFriend(Integer id, Integer idFriend) {
        log.debug(String.format("Добавление в друзья пользователю c id = %d пользователя с id = %d", id, idFriend));
        isContainsUser(id);
        isContainsUser(idFriend);

        userStorage.addFriend(id, idFriend);
    }

    public void removeFriend(Integer id, Integer idFriend) {
        log.debug(String.format("Удаление из друзей пользователя c id = %d пользователя с id = %d", id, idFriend));
        isContainsUser(id);
        isContainsUser(idFriend);
        userStorage.removeFriend(id, idFriend);
    }

    public List<User> getFriends(Integer id) {
        log.debug(String.format("Выдача списка друзей пользователя c id = %d", id));
        isContainsUser(id);
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Integer id, Integer idOther) {
        log.debug(String.format("Поиск общих друзей пользователя c id = %d  и пользователя с id = %d", id, idOther));
        isContainsUser(id);
        isContainsUser(idOther);

        return userStorage.getCommonFriend(id, idOther);
    }

    private User validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return user;
    }

    public void isContainsUser(Integer id) {
        userStorage.isContains(id);
    }
}
