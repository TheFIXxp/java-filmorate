package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("addFilm: new film -> film id generated")
    void addFilm_newFilm_idGenerated() {
        Film film = TestDataFactory.createValidFilm();
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);
        film.setGenres(Set.of());

        Film addedFilm = this.filmStorage.addFilm(film);

        assertNotNull(addedFilm);
        assertNotEquals(0, addedFilm.getId());
        assertEquals("Film", addedFilm.getName());
        assertEquals("Description", addedFilm.getDescription());
        assertEquals(90, addedFilm.getDuration());
    }

    @Test
    @DisplayName("addFilm: film without mpa -> mpa is null")
    void addFilm_filmWithoutMpa_mpaIsNull() {
        Film film = TestDataFactory.createValidFilm();
        film.setMpa(null);
        film.setGenres(Set.of());

        Film addedFilm = this.filmStorage.addFilm(film);

        assertNotNull(addedFilm);
        assertNotEquals(0, addedFilm.getId());
        assertNull(addedFilm.getMpa());
    }

    @Test
    @DisplayName("addFilm: multiple films -> unique ids generated")
    void addFilm_multipleFilms_uniqueIdsGenerated() {
        Film film1 = TestDataFactory.createValidFilm();
        Film film2 = TestDataFactory.createValidFilm();
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film1.setMpa(mpa);
        film1.setGenres(Set.of());
        film2.setMpa(mpa);
        film2.setGenres(Set.of());

        Film added1 = this.filmStorage.addFilm(film1);
        Film added2 = this.filmStorage.addFilm(film2);

        assertNotEquals(added1.getId(), added2.getId());
        assertTrue(added1.getId() > 0);
        assertTrue(added2.getId() > 0);
    }

    @Test
    @DisplayName("addFilm: film with genres -> genres saved")
    void addFilm_filmWithGenres_genresSaved() {
        Film film = TestDataFactory.createValidFilm();
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre genre1 = new Genre();
        genre1.setId(1);
        genre1.setName("Комедия");
        Genre genre2 = new Genre();
        genre2.setId(2);
        genre2.setName("Драма");
        film.setGenres(Set.of(genre1, genre2));

        Film addedFilm = this.filmStorage.addFilm(film);

        assertNotNull(addedFilm);
        assertNotEquals(0, addedFilm.getId());
        assertEquals(2, addedFilm.getGenres().size());
    }

    @Test
    @DisplayName("updateFilm: existing film -> film updated")
    void updateFilm_existingFilm_filmUpdated() {
        Film originalFilm = TestDataFactory.createValidFilm();
        Mpa mpa = new Mpa();
        mpa.setId(1);
        originalFilm.setMpa(mpa);
        originalFilm.setGenres(Set.of());
        Film addedFilm = this.filmStorage.addFilm(originalFilm);
        long filmId = addedFilm.getId();

        Film updatedFilm = new Film();
        updatedFilm.setId(filmId);
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(originalFilm.getReleaseDate());
        updatedFilm.setDuration(150);
        updatedFilm.setMpa(mpa);
        updatedFilm.setGenres(Set.of());

        Film result = this.filmStorage.updateFilm(updatedFilm);

        assertEquals(filmId, result.getId());
        assertEquals("Updated Film", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(150, result.getDuration());
    }

    @Test
    @DisplayName("getFilmById: film exists -> return film")
    void getFilmById_filmExists_returnFilm() {
        Film film = TestDataFactory.createValidFilm();
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);
        film.setGenres(Set.of());
        Film addedFilm = this.filmStorage.addFilm(film);
        long filmId = addedFilm.getId();

        Optional<Film> foundFilm = this.filmStorage.getFilmById(filmId);

        assertTrue(foundFilm.isPresent());
        assertEquals(filmId, foundFilm.get().getId());
        assertEquals("Film", foundFilm.get().getName());
    }

    @Test
    @DisplayName("getFilmById: film does not exist -> return empty")
    void getFilmById_filmDoesNotExist_returnEmpty() {
        Optional<Film> foundFilm = this.filmStorage.getFilmById(9999);

        assertFalse(foundFilm.isPresent());
    }

    @Test
    @DisplayName("getFilms: films exist -> return all films")
    void getFilms_filmsExist_returnAllFilms() {
        Film film1 = TestDataFactory.createValidFilm();
        Film film2 = TestDataFactory.createValidFilm();
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film1.setMpa(mpa);
        film1.setGenres(Set.of());
        film2.setMpa(mpa);
        film2.setGenres(Set.of());

        this.filmStorage.addFilm(film1);
        this.filmStorage.addFilm(film2);

        Collection<Film> films = this.filmStorage.getFilms();

        assertNotNull(films);
        assertEquals(2, films.size());
    }

    @Test
    @DisplayName("getFilms: no films -> return empty collection")
    void getFilms_noFilms_returnEmptyCollection() {
        Collection<Film> films = this.filmStorage.getFilms();

        assertNotNull(films);
        assertTrue(films.isEmpty());
    }

    @Test
    @DisplayName("addFilm: film with null description -> description is null")
    void addFilm_filmWithNullDescription_descriptionIsNull() {
        Film film = TestDataFactory.createValidFilm();
        film.setDescription(null);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);
        film.setGenres(Set.of());

        Film addedFilm = this.filmStorage.addFilm(film);

        Optional<Film> foundFilm = this.filmStorage.getFilmById(addedFilm.getId());

        assertTrue(foundFilm.isPresent());
        assertNull(foundFilm.get().getDescription());
    }
}


