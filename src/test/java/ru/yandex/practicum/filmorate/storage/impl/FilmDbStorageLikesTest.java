package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmDbStorageLikesTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Test
    @DisplayName("addLike: film exists -> like added")
    void addLike_filmExists_likeAdded() {
        Film film = TestDataFactory.createValidFilm();
        Film addedFilm = this.filmStorage.addFilm(film);
        long filmId = addedFilm.getId();
        User user1 = this.userStorage.addUser(TestDataFactory.createValidUser());

        this.filmStorage.addLike(filmId, user1.getId());

        int likesCount = this.filmStorage.getLikesCount(filmId);
        assertEquals(1, likesCount);
    }

    @Test
    @DisplayName("addLike: same like twice -> no duplicates")
    void addLike_sameLikeTwice_noDuplicates() {
        Film film = TestDataFactory.createValidFilm();
        Film addedFilm = this.filmStorage.addFilm(film);
        long filmId = addedFilm.getId();
        User user1 = this.userStorage.addUser(TestDataFactory.createValidUser());

        this.filmStorage.addLike(filmId, user1.getId());
        this.filmStorage.addLike(filmId, user1.getId());

        int likesCount = this.filmStorage.getLikesCount(filmId);
        assertEquals(1, likesCount);
    }

    @Test
    @DisplayName("removeLike: like exists -> like removed")
    void removeLike_likeExists_likeRemoved() {
        Film film = TestDataFactory.createValidFilm();
        Film addedFilm = this.filmStorage.addFilm(film);
        long filmId = addedFilm.getId();
        User user1 = this.userStorage.addUser(TestDataFactory.createValidUser());

        this.filmStorage.addLike(filmId, user1.getId());
        this.filmStorage.removeLike(filmId, user1.getId());

        int likesCount = this.filmStorage.getLikesCount(filmId);
        assertEquals(0, likesCount);
    }

    @Test
    @DisplayName("getLikesCount: film has likes -> return correct count")
    void getLikesCount_filmHasLikes_returnCorrectCount() {
        Film film = TestDataFactory.createValidFilm();
        Film addedFilm = this.filmStorage.addFilm(film);
        long filmId = addedFilm.getId();
        User user1 = this.userStorage.addUser(TestDataFactory.createValidUser());
        User user2 = this.userStorage.addUser(TestDataFactory.createValidUser2());
        User user3 = this.userStorage.addUser(TestDataFactory.createValidUser3());

        this.filmStorage.addLike(filmId, user1.getId());
        this.filmStorage.addLike(filmId, user2.getId());
        this.filmStorage.addLike(filmId, user3.getId());

        int likesCount = this.filmStorage.getLikesCount(filmId);

        assertEquals(3, likesCount);
    }

    @Test
    @DisplayName("getLikesCount: film has no likes -> return zero")
    void getLikesCount_filmHasNoLikes_returnZero() {
        Film film = TestDataFactory.createValidFilm();
        Film addedFilm = this.filmStorage.addFilm(film);
        long filmId = addedFilm.getId();

        int likesCount = this.filmStorage.getLikesCount(filmId);

        assertEquals(0, likesCount);
    }
}

