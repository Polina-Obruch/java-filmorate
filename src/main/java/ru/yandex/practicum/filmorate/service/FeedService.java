package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedDbStorage;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class FeedService {
    FeedDbStorage feedDbStorage;

    public List<Event> getUserFeed(Integer id) {
        log.debug(String.format("Выдача ленты новостей для пользоватесля с id = %d", id));
        return feedDbStorage.getFeed(id);
    }

    public void saveEventAddFriend(Integer id, Integer friendId) {
        log.debug(String.format("Сохранение события - добавить в друзья - для пользоватесля с id = %d", id));
        feedDbStorage.addFriend(id, friendId);

    }

    public void saveEventRemoveFriend(Integer id, Integer friendId) {
        log.debug(String.format("Сохранение события - удалить из друзей - для пользоватесля с id = %d", id));
        feedDbStorage.removeFriend(id, friendId);

    }

    public void saveEventAddLikeFilm(Integer filmId, Integer userId) {
        log.debug(String.format("Сохранение события - поставил лайк - для пользоватесля с id = %d", userId));
        feedDbStorage.addLike(filmId, userId);

    }

    public void saveEventRemoveLikeFilm(Integer filmId, Integer userId) {
        log.debug(String.format("Сохранение события - удалить лайк - для пользоватесля с id = %d", userId));
        feedDbStorage.removeLike(filmId, userId);

    }

    public void saveEventAddReview(Integer reviewId, Integer userId) {
        log.debug(String.format("Сохранение события - добавить отзыв - для пользоватесля с id = %d", userId));
        feedDbStorage.addReview(reviewId, userId);

    }

    public void saveEventRemoveReview(Integer reviewId, Integer userId) {
        log.debug(String.format("Сохранение события - удалить отзыв - для пользоватесля с id = %d", userId));
        feedDbStorage.removeReview(reviewId, userId);

    }

    public void saveEventUpdateReview(Integer reviewId, Integer userId) {
        log.debug(String.format("Сохранение события - обновить отзыв - для пользоватесля с id = %d", userId));
        feedDbStorage.updateReview(reviewId, userId);

    }
}
