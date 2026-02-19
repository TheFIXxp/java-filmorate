package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.testutil.TestConstants;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("validate: valid user -> no violations")
    void validate_validUser_noViolations() {
        User user = TestDataFactory.createValidUser();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Expected no violations, but got: " + violations);
    }

    @Test
    @DisplayName("validate: invalid email -> violation")
    void validate_invalidEmail_violation() {
        User user = TestDataFactory.createValidUser();
        user.setEmail(TestConstants.USER_INVALID_EMAIL);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertHasViolation(violations, TestConstants.USER_EMAIL_PROPERTY);
    }

    @Test
    @DisplayName("validate: blank login -> violation")
    void validate_blankLogin_violation() {
        User user = TestDataFactory.createValidUser();
        user.setLogin(TestConstants.BLANK);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertHasViolation(violations, TestConstants.USER_LOGIN_PROPERTY);
    }

    @Test
    @DisplayName("validate: login with spaces -> violation")
    void validate_loginWithSpaces_violation() {
        User user = TestDataFactory.createValidUser();
        user.setLogin(TestConstants.USER_LOGIN_WITH_SPACE);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertHasViolation(violations, TestConstants.USER_LOGIN_PROPERTY);
    }

    @Test
    @DisplayName("validate: null birthday -> violation")
    void validate_nullBirthday_violation() {
        User user = TestDataFactory.createValidUser();
        user.setBirthday(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertHasViolation(violations, TestConstants.USER_BIRTHDAY_PROPERTY);
    }

    @Test
    @DisplayName("validate: future birthday -> violation")
    void validate_futureBirthday_violation() {
        User user = TestDataFactory.createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertHasViolation(violations, TestConstants.USER_BIRTHDAY_PROPERTY);
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
