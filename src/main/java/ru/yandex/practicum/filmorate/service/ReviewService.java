package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;
    private final FeedService feedService;

    public Review addReview(Review review) {
        log.debug("Сохранение отзыва");
        filmService.isFilmContains(review.getFilmId());
        userService.isContainsUser(review.getUserId());
        Review saveReview = reviewStorage.add(review);
        feedService.saveEventAddReview(saveReview.getReviewId(), saveReview.getUserId());
        return saveReview;
    }

    public Review updateReview(Review review) {
        log.debug(String.format("Обновление отзыва с id = %d", review.getReviewId()));
        Review saveReview = reviewStorage.update(review);
        feedService.saveEventUpdateReview(saveReview.getReviewId(), saveReview.getUserId());
        return saveReview;
    }

    public Review getReview(Integer id) {
        log.debug(String.format("Выдача отзыва с id = %d", id));
        return reviewStorage.get(id);
    }

    public void removeReview(Integer id) {
        log.debug(String.format("Удаляем отзыв с id = %d", id));
        Integer userId = reviewStorage.remove(id);
        feedService.saveEventRemoveReview(id, userId);
    }

    public List<Review> getAllReviews(Integer filmId, Integer count) {
        List<Review> reviews;

        if (count <= 0) {
            throw new IncorrectParameterException("Значение параметра count должно быть больше нуля");
        }

        if (filmId != null && filmId > 0) {
            reviews = this.getAllReviewsByFilmId(filmId, count);
        } else if (filmId == null) {
            reviews = this.getAllReviewsWithCount(count);
        } else {
            throw new IncorrectParameterException("Значение параметра filmId должно быть больше нуля");
        }

        return reviews;
    }

    private List<Review> getAllReviewsWithCount(Integer count) {
        log.debug(String.format("Выдача списка всех отзывов с count = %d", count));
        return reviewStorage.getAllReviews(count);
    }

    private List<Review> getAllReviewsByFilmId(Integer filmId, Integer count) {
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
