package ru.yandex.practicum.filmorate.storage;


import ru.yandex.practicum.filmorate.model.User;

import java.util.List;


public interface UserStorage {

    User add(User user);

    void remove(Integer id);

    User update(User user);

    User get(Integer id);

    List<User> getAll();

    void addFriend(Integer id, Integer idFriend);

    void removeFriend(Integer id, Integer idFriend);

    List<User> getFriends(Integer id);

    List<User> getCommonFriend(Integer id, Integer idOther);

    void isContains(Integer id);
}
