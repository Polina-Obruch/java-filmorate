package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class Director {

    private Integer id;

    @NotBlank(message = "Имя Режиссёра не может быть пустым")
    @NotNull(message = "Имя Режиссёра не может быть null")
    private String name;
}
