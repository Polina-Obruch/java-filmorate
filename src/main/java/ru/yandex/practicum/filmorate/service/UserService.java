package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private final Map<Integer, User> users = new HashMap<>();

    public User addUser(int id, User user) {
        User saveUser = validateName(user);
        saveUser.setId(id);
        users.put(id, saveUser);
        return saveUser;
    }

    public User updateUser(int id, User user) {
        User saveUser = validateName(user);
        users.put(id, saveUser);
        return saveUser;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public boolean isContains(int id) {
        return users.containsKey(id);
    }

    private User validateName(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        return user;
    }
}
