package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {
    private int id;

    @NotNull(message = "Email не может быть пустым")
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email введен с ошибкой")
    private String email;

    @NotNull(message = "Логин не может быть пустым")
    @NotBlank(message = "Логин не может быть пустым")
    private String login;
    private String name;

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
