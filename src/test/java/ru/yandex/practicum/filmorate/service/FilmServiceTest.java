package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.impl.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.impl.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmServiceTest {

    private FilmService filmService;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        this.filmStorage = new InMemoryFilmStorage();
        this.userStorage = new InMemoryUserStorage();
        this.filmService = new FilmService(this.filmStorage, this.userStorage);
    }

    @Test
    @DisplayName("getFilmById: film does not exist -> throw NotFoundException")
    void getFilmById_filmDoesNotExist_throwNotFoundException() {
        assertThrows(NotFoundException.class, () -> this.filmService.getFilmById(999L));
    }

    @Test
    @DisplayName("updateFilm: film does not exist -> throw NotFoundException")
    void updateFilm_filmDoesNotExist_throwNotFoundException() {
        Film film = TestDataFactory.createValidFilm();
        film.setId(999L);
        assertThrows(NotFoundException.class, () -> this.filmService.updateFilm(film));
    }

    @Test
    @DisplayName("addLike: film does not exist -> throw NotFoundException")
    void addLike_filmDoesNotExist_throwNotFoundException() {
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());
        assertThrows(NotFoundException.class, () -> this.filmService.addLike(999L, user.getId()));
    }

    @Test
    @DisplayName("addLike: user does not exist -> throw NotFoundException")
    void addLike_userDoesNotExist_throwNotFoundException() {
        Film film = this.filmStorage.addFilm(TestDataFactory.createValidFilm());
        assertThrows(NotFoundException.class, () -> this.filmService.addLike(film.getId(), 999L));
    }

    @Test
    @DisplayName("removeLike: film does not exist -> throw NotFoundException")
    void removeLike_filmDoesNotExist_throwNotFoundException() {
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());
        assertThrows(NotFoundException.class, () -> this.filmService.removeLike(999L, user.getId()));
    }

    @Test
    @DisplayName("removeLike: user does not exist -> throw NotFoundException")
    void removeLike_userDoesNotExist_throwNotFoundException() {
        Film film = this.filmStorage.addFilm(TestDataFactory.createValidFilm());
        assertThrows(NotFoundException.class, () -> this.filmService.removeLike(film.getId(), 999L));
    }

    @Test
    @DisplayName("addFilm: release date is too early -> throw ValidationException")
    void addFilm_releaseDateIsTooEarly_throwValidationException() {
        Film film = TestDataFactory.createValidFilm();
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        assertThrows(jakarta.validation.ValidationException.class, () -> this.filmService.addFilm(film));
    }

    @Test
    @DisplayName("getPopular: return films sorted by likes count")
    void getPopular_returnFilmsSortedByLikesCount() {
        Film film1 = this.filmStorage.addFilm(TestDataFactory.createValidFilm());
        Film film2 = this.filmStorage.addFilm(TestDataFactory.createValidFilm());
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());

        this.filmService.addLike(film2.getId(), user.getId());

        List<Film> popular = (List<Film>) this.filmService.getPopular(10);

        assertEquals(2, popular.size());
        assertEquals(film2.getId(), popular.get(0).getId());
        assertEquals(film1.getId(), popular.get(1).getId());
    }
}
