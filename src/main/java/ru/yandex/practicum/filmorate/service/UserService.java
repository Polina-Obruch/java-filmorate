package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private Integer countId;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
        this.countId = 0;
    }

    public User getUser(Integer id) {
        User user = userStorage.get(id);

        if (user == null) {
            log.debug(String.format("Пользователь с id = %d не был найден в базе", id));
            throw new UserNotFoundException(String.format("Пользователь с id = %d не был найден в базе", id));
        }

        return user;
    }

    public User addUser(User user) {
        User saveUser = validateName(user);
        Integer id = getId();
        saveUser.setId(id);
        userStorage.add(id, saveUser);
        return saveUser;
    }

    public User updateUser(User user) {
        User saveUser = validateName(user);
        Integer id = saveUser.getId();
        if (userStorage.get(id) != null) {
            userStorage.add(id, saveUser);
            return saveUser;
        }

        log.debug(String.format("Пользователь с id = %d не был обновлен, так как не найден в базе", id));
        throw new UserNotFoundException(String.format("Пользователь с id = %d не был найден в базе", id));
    }

    public List<User> getUsers() {
        return userStorage.getAll();
    }

    public void addFriend(Integer id, Integer friendId) {
        User user = userStorage.get(id);
        User friendUser = userStorage.get(friendId);

        if (user == null) {
            log.debug(String.format("Для пользователя с id = %d не был добавлен друг", id));
            throw new UserNotFoundException(String.format("Пользователь с id = %d не был найден в базе", id));
        } else if (friendUser == null) {
            log.debug(String.format("Пользователь с id = %d не был добавлен как друг", friendId));
            throw new UserNotFoundException(String.format("Пользователь с id = %d не был найден в базе", friendId));
        }

        Set<Integer> userFriends = user.getFriends();
        Set<Integer> friendUserFriends = friendUser.getFriends();

        if (userFriends == null) {
            userFriends = new HashSet<>();
            user.setFriends(userFriends);
        }

        if (friendUserFriends == null) {
            friendUserFriends = new HashSet<>();
            friendUser.setFriends(friendUserFriends);
        }

        userFriends.add(friendId);
        friendUserFriends.add(id);
    }

    public void removeFriend(Integer id, Integer friendId) {
        User user = userStorage.get(id);
        User friendUser = userStorage.get(friendId);

        if (user == null) {
            log.debug(String.format("Для пользователя с id = %d не был удален друг", id));
            throw new UserNotFoundException(String.format("Пользователь с id = %d не был найден в базе", id));
        } else if (friendUser == null) {
            log.debug(String.format("Пользователь с id = %d не был удален как друг", friendId));
            throw new UserNotFoundException(String.format("Пользователь с id = %d не был найден в базе", friendId));
        }

        Set<Integer> userFriends = user.getFriends();
        Set<Integer> friendUserFriends = friendUser.getFriends();

        if (userFriends != null && friendUserFriends != null) {
            userFriends.remove(friendId);
            friendUserFriends.remove(id);
        }
    }

    public List<User> getAllFriends(Integer id) {
        log.debug("Начинаем выдачу списка друзей");
        Set<Integer> userFriend = this.getUser(id).getFriends();

        if (userFriend == null) {
            log.debug(String.format("Пользователю с id = %d был выдан пустой список друзей", id));
            return new ArrayList<>();
        }

        log.debug(String.format("Пользователю с id = %d был выдан список друзей", id));
        return userFriend.stream().map(userStorage::get).collect(Collectors.toList());
    }

    public List<User> getCommonFriend(Integer id, Integer otherId) {
        log.debug("Начинаем поиск общих друзей");

        Set<Integer> userFriend = this.getUser(id).getFriends();
        Set<Integer> otherUserFriends = this.getUser(otherId).getFriends();

        if (otherUserFriends == null || userFriend == null) {
            log.debug("Выдан пустой список общих друзей, так списки друзей пользователей пусты");
            return new ArrayList<>();
        }

        Set<Integer> common = new HashSet<>(userFriend);
        common.retainAll(otherUserFriends);
        log.debug(String.format("Выдан список общих друзей id  = %d c otherId = %d",id, otherId));
        return common.stream().map(userStorage::get).collect(Collectors.toList());
    }

    private User validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return user;
    }

    private Integer getId() {
        return ++countId;
    }
}
