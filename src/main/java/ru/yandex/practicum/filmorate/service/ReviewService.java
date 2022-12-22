package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ReviewService {
    ReviewStorage reviewStorage;
    FilmService filmService;
    UserService userService;

    public Review addReview(Review review) {
        log.debug("Сохранение отзыва");
        filmService.isFilmContains(review.getFilmId());
        userService.isContainsUser(review.getUserId());
        return reviewStorage.add(review);
    }

    public Review updateReview(Review review) {
        log.debug(String.format("Обновление отзыва с id = %d", review.getReviewId()));
        return reviewStorage.update(review);
    }

    public Review getReview(Integer id) {
        log.debug(String.format("Выдача отзыва с id = %d", id));
        return reviewStorage.get(id);
    }

    public void removeReview(Integer id) {
        log.debug(String.format("Удаляем отзыв с id = %d", id));
        reviewStorage.remove(id);
    }

    public List<Review> getAllReviews(Integer count) {
        log.debug(String.format("Выдача списка всех отзывов с count = %d", count));
        return reviewStorage.getAllReviews(count);
    }

    public List<Review> getAllReviewsByFilmId(Integer filmId, Integer count) {
        log.debug(String.format("Выдача списка отзывов для фильма с id = %d и  count = %d", filmId, count));
        filmService.isFilmContains(filmId);

        return reviewStorage.getAllReviewsByFilmId(filmId, count);
    }

    public void addLike(Integer id, Integer userId) {
        log.debug(String.format("Добавление лайка отзыву с id = %d от пользователя с id = %d", id, userId));
        isReviewContains(id);
        userService.isContainsUser(userId);
        reviewStorage.addLike(id, userId);

    }

    public void addDislike(Integer id, Integer userId) {
        log.debug(String.format("Добавление дизлайка отзыву с id = %d от пользователя с id = %d", id, userId));
        isReviewContains(id);
        userService.isContainsUser(userId);
        reviewStorage.addDislike(id, userId);
    }

    public void removeLike(Integer id, Integer userId) {
        log.debug(String.format("Удаление лайка отзыву с id = %d от пользователя с id = %d", id, userId));
        isReviewContains(id);
        userService.isContainsUser(userId);
        reviewStorage.removeLike(id, userId);
    }

    public void removeDislike(Integer id, Integer userId) {
        log.debug(String.format("Удаление дизлайка отзыву с id = %d от пользователя с id = %d", id, userId));
        isReviewContains(id);
        userService.isContainsUser(userId);
        reviewStorage.removeDislike(id, userId);
    }

    public void isReviewContains(Integer id) {
        reviewStorage.isReviewContains(id);
    }
}
