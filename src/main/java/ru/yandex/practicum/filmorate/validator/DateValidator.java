package ru.yandex.practicum.filmorate.validator;

import ru.yandex.practicum.filmorate.validator.AfterData;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateValidator implements ConstraintValidator<AfterData, LocalDate> {
    private static final LocalDate MIN_DATE_RELEASE = LocalDate.parse("1895-12-28");

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        return date.isAfter(MIN_DATE_RELEASE);
    }
}
