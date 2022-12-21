package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewDbStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ReviewService {
    ReviewDbStorage reviewStorage;
    FilmService filmService;
    UserService userService;

    public Review addReview(Review review) {
        log.debug("Сохранение отзыва");
        filmService.isFilmContains(review.getFilmId());
        userService.isContainsUser(review.getUserId());
        return reviewStorage.add(review);
    }

    public Review updateReview(Review review) {
        log.debug(String.format("Обновление отзыва с reviewId = %d", review.getReviewId()));
        return reviewStorage.update(review);
    }

    public Review getReview(Integer id) {
        log.debug(String.format("Выдача отзыва с reviewId = %d", id));
        return reviewStorage.get(id);
    }

    public void removeReview(Integer id) {
        log.debug(String.format("Удаляем отзыв с id =%d", id));
        reviewStorage.remove(id);
    }

    public List<Review> getAllReviews() {
        return null;
    }
}
