package ru.yandex.practicum.filmorate.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Setter
@Getter
@AllArgsConstructor
public class Genre {
    private Integer id;
    private String name;

    //Рекоммендовали не использовать @Data
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        if (name == null && genre.name == null) {
            return id.equals(genre.id);
        } else if ((name == null || genre.name == null)) {
            return false;
        }
        return id.equals(genre.id) && name.equals(genre.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
