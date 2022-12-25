package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review add(Review review);

    Review update(Review review);

    Review get(Integer id);

    Integer remove(Integer id);

    List<Review> getAllReviews(Integer count);

    List<Review> getAllReviewsByFilmId(Integer filmId, Integer count);

    void isReviewContains(Integer id);

    void addLike(Integer id, Integer userId);

    void addDislike(Integer id, Integer userId);

    void removeLike(Integer id, Integer userId);

    void removeDislike(Integer id, Integer userId);
}
