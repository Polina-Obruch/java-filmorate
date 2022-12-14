package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Positive;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Mpa {
    @Positive(message = "Идендификатор MPA не может быть отрицательным")
    private Integer id;
    private String name;

}
