package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@RequestBody @Valid Review review) {
        Review saveReview = reviewService.addReview(review);
        log.debug(String.format("Новый отзыв был добавлен. Выданный id = %d", saveReview.getReviewId()));
        return saveReview;
    }

    @PutMapping
    public Review updateReview(@RequestBody @Valid Review review) {
        Review saveReview = reviewService.updateReview(review);
        log.debug(String.format("Отзыв с id = %d был обновлен", saveReview.getReviewId()));
        return saveReview;
    }


    @GetMapping("/{id}")
    public Review getReview(@PathVariable Integer id) {
        Review saveReview = reviewService.getReview(id);
        log.debug(String.format("Отзыв с id = %d был выдан", saveReview.getReviewId()));
        return saveReview;
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable Integer id) {
        reviewService.removeReview(id);
        log.debug(String.format("Отзыв с id = %d удален", id));
    }

    @GetMapping
    public List<Review> getAllReviews(@RequestParam(required = false) Integer filmId,
                                      @RequestParam(defaultValue = "10", required = false) Integer count) {
        List<Review> reviews = reviewService.getAllReviews(filmId, count);
        log.debug("Список всех отзывов был выдан");
        return reviews;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.addLike(id, userId);
        log.debug(String.format("Отзыву с id = %d был поставлен лайк от пользователя с id = %d", id, userId));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.addDislike(id, userId);
        log.debug(String.format("Отзыву с id = %d был поставлен дизлайк от пользователя с id = %d", id, userId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.removeLike(id, userId);
        log.debug(String.format("У отзыва с id = %d был удален лайк от пользователя с id = %d", id, userId));
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.removeDislike(id, userId);
        log.debug(String.format("У отзыва с id = %d был удален дизлайк от пользователя с id = %d", id, userId));
    }
}
