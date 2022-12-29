package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class Review {

    private Integer reviewId;

    @NotBlank(message = "Отзыв не может быть пустым.")
    private String content;

    @NotNull(message = "Тип отзыва не может быть пустым")
    private Boolean isPositive;

    @NotNull(message = "Id пользователя не может быть пустым")
    private Integer userId;

    @NotNull(message = "Id фильма не может быть пустым")
    private Integer filmId;
    private Integer useful;

    public Review(Integer reviewId) {
        this.reviewId = reviewId;
    }
}
