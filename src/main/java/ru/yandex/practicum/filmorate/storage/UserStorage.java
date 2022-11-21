package ru.yandex.practicum.filmorate.storage;


import ru.yandex.practicum.filmorate.model.User;

import java.util.List;


public interface UserStorage {

    void add(Integer id, User user);

    void remove(Integer id);

    User get(Integer id);

    List<User> getAll();

    boolean isContains(Integer id);
}
