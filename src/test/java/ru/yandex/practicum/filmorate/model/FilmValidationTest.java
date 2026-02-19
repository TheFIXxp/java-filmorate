package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.testutil.TestConstants;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("validate: valid film -> no violations")
    void validate_validFilm_noViolations() {
        Film film = TestDataFactory.createValidFilm();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Expected no violations, but got: " + violations);
    }

    @Test
    @DisplayName("validate: blank name -> violation")
    void validate_blankName_violation() {
        Film film = TestDataFactory.createValidFilm();
        film.setName(TestConstants.BLANK);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertHasViolation(violations, TestConstants.FILM_NAME_PROPERTY);
    }

    @Test
    @DisplayName("validate: null name -> violation")
    void validate_nullName_violation() {
        Film film = TestDataFactory.createValidFilm();
        film.setName(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertHasViolation(violations, TestConstants.FILM_NAME_PROPERTY);
    }

    @Test
    @DisplayName("validate: description length 200 -> no violations")
    void validate_descriptionLength200_noViolations() {
        Film film = TestDataFactory.createValidFilm();
        film.setDescription("a".repeat(TestConstants.DESCRIPTION_MAX_LENGTH));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Expected no violations, but got: " + violations);
    }

    @Test
    @DisplayName("validate: description length 201 -> violation")
    void validate_descriptionLength201_violation() {
        Film film = TestDataFactory.createValidFilm();
        film.setDescription("a".repeat(TestConstants.DESCRIPTION_TOO_LONG_LENGTH));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertHasViolation(violations, TestConstants.FILM_DESCRIPTION_PROPERTY);
    }

    @Test
    @DisplayName("validate: null release date -> violation")
    void validate_nullReleaseDate_violation() {
        Film film = TestDataFactory.createValidFilm();
        film.setReleaseDate(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertHasViolation(violations, TestConstants.FILM_RELEASE_DATE_PROPERTY);
    }

    @Test
    @DisplayName("validate: non-positive duration -> violation")
    void validate_nonPositiveDuration_violation() {
        Film film = TestDataFactory.createValidFilm();
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertHasViolation(violations, TestConstants.FILM_DURATION_PROPERTY);
    }

    private static <T> void assertHasViolation(Set<ConstraintViolation<T>> violations, String property) {
        assertFalse(
                violations.isEmpty(),
                "Expected violation for property: " + property + ", but got none"
        );
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property)),
                "Expected violation for property: " + property + ", but got: " + violations
        );
    }
}
