package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.validator.NoSpaces;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email введен с ошибкой")
    private String email;

    private String name;

    //@NotBlank не нужен, так как @NoSpaces отловит все возможные комбинации пробелов
    @NotNull(message = "Логин не может быть пустым")
    @NoSpaces
    private String login;

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id) && email.equals(user.email) && name.equals(user.name)
                && login.equals(user.login) && birthday.equals(user.birthday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, name, login, birthday);
    }
}
