package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SpaceValidator implements ConstraintValidator<NoSpaces, String> {
    private static final String CONTAINS_CHAR = " ";

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

        //Отлавливаем другой анотацией
        if (s == null) {
            return true;
        }
        return !s.contains(CONTAINS_CHAR);
    }
}
