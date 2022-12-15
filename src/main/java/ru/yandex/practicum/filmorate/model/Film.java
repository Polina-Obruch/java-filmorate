package ru.yandex.practicum.filmorate.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.filmorate.validator.AfterData;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;


@Getter
@Setter
@NoArgsConstructor
public class Film {
    private Integer id;

    @JsonIgnore
    private Integer likes;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    private String description;

    @AfterData
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма не может быть отрицательным")
    private Integer duration;

    private Mpa mpa;
    private LinkedHashSet<Genre> genres;

    public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration, Mpa mpa, LinkedHashSet<Genre> genres) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.genres = genres;
    }
}
