package ru.yandex.practicum.filmorate.testutil;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class TestConstants {

    public static final String FILM_NAME = "Film";
    public static final String FILM_DESCRIPTION = "Description";
    public static final LocalDate FILM_RELEASE_DATE = LocalDate.of(2000, 1, 1);
    public static final int FILM_DURATION = 90;

    public static final int DESCRIPTION_MAX_LENGTH = 200;
    public static final int DESCRIPTION_TOO_LONG_LENGTH = 201;
    public static final String BLANK = " ";

    public static final String FILM_NAME_PROPERTY = "name";
    public static final String FILM_DESCRIPTION_PROPERTY = "description";
    public static final String FILM_RELEASE_DATE_PROPERTY = "releaseDate";
    public static final String FILM_DURATION_PROPERTY = "duration";

    public static final String USER_EMAIL = "user@example.com";
    public static final String USER_INVALID_EMAIL = "invalid-email";
    public static final String USER_LOGIN = "login";
    public static final String USER_LOGIN_WITH_SPACE = "my login";
    public static final String USER_NAME = "Name";
    public static final LocalDate USER_BIRTHDAY = LocalDate.of(1990, 1, 1);

    public static final String USER_EMAIL_PROPERTY = "email";
    public static final String USER_LOGIN_PROPERTY = "login";
    public static final String USER_BIRTHDAY_PROPERTY = "birthday";
}
