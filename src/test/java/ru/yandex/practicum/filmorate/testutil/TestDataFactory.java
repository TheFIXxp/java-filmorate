package ru.yandex.practicum.filmorate.testutil;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

@UtilityClass
public class TestDataFactory {

    public static Film createValidFilm() {
        Film film = new Film();
        film.setName(TestConstants.FILM_NAME);
        film.setDescription(TestConstants.FILM_DESCRIPTION);
        film.setReleaseDate(TestConstants.FILM_RELEASE_DATE);
        film.setDuration(TestConstants.FILM_DURATION);
        return film;
    }

    public static User createValidUser() {
        User user = new User();
        user.setEmail(TestConstants.USER_EMAIL);
        user.setLogin(TestConstants.USER_LOGIN);
        user.setName(TestConstants.USER_NAME);
        user.setBirthday(TestConstants.USER_BIRTHDAY);
        return user;
    }

    public static User createValidUser2() {
        User user = new User();
        user.setEmail(TestConstants.USER_EMAIL_2);
        user.setLogin(TestConstants.USER_LOGIN_2);
        user.setName(TestConstants.USER_NAME_2);
        user.setBirthday(TestConstants.USER_BIRTHDAY_2);
        return user;
    }

    public static User createValidUser3() {
        User user = new User();
        user.setEmail(TestConstants.USER_EMAIL_3);
        user.setLogin(TestConstants.USER_LOGIN_3);
        user.setName(TestConstants.USER_NAME_3);
        user.setBirthday(TestConstants.USER_BIRTHDAY_3);
        return user;
    }
}
