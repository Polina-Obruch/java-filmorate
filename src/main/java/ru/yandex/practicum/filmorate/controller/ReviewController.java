package ru.yandex.practicum.filmorate.controller;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
public class ReviewController {
    ReviewService service;

    @PostMapping
    public Review addReview(@RequestBody @Valid Review review) {
        Review saveReview = service.addReview(review);
        log.debug(String.format("Новый отзыв был добавлен. Выданный id = %d", saveReview.getReviewId()));
        return saveReview;
    }

    @PutMapping
    public Review updateReview(@RequestBody @Valid Review review) {
        Review saveReview = service.updateReview(review);
        log.debug(String.format("Отзыв с id = %d был обновлен", saveReview.getReviewId()));
        return saveReview;
    }


    @GetMapping("/{id}")
    public Review getReview(@PathVariable Integer id) {
        Review saveReview = service.getReview(id);
        log.debug(String.format("Отзыв с id = %d был выдан", saveReview.getReviewId()));
        return saveReview;
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable Integer id) {
        service.removeReview(id);
        log.debug(String.format("Отзыв с id = %d удален", id));
    }

    @GetMapping
    public List<Review> getAllReviews(@RequestParam(required = false) Integer filmId,
                                      @RequestParam(defaultValue = "10", required = false) Integer count) {
        List<Review> reviews;

        if (count <= 0) {
            throw new IncorrectParameterException("Значение параметра count должно быть больше нуля");
        }

        if (filmId != null && filmId > 0) {
            reviews = service.getAllReviewsByFilmId(filmId,count);
        } else if (filmId == null) {
            reviews = service.getAllReviews(count);
        } else {
            throw new IncorrectParameterException("Значение параметра filmId должно быть больше нуля");
        }

        log.debug("Список всех отзывов был выдан");
        return reviews;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        service.addLike(id,userId);
        log.debug(String.format("Отзыву с id = %d был поставлен лайк", id));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        service.addDislike(id,userId);
        log.debug(String.format("Отзыву с id = %d был поставлен дизлайк", id));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        service.removeLike(id,userId);
        log.debug(String.format("У отзыва с id = %d был удален лайк", id));
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        service.removeDislike(id, userId);
        log.debug(String.format("У отзыва с id = %d был удален дизлайк", id));
    }
}
