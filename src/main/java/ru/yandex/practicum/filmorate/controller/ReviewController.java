package ru.yandex.practicum.filmorate.controller;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
    public List<Review> getAllReviews() {
        List<Review> reviews = service.getAllReviews();
        log.debug("Список всех отзывов был выдан");
        return reviews;
    }

}
