package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    @DisplayName("getAll: return all genres")
    void getAll_returnAllGenres() {
        List<Genre> genres = this.genreStorage.getAll();

        assertNotNull(genres);
        assertEquals(6, genres.size());
    }

    @Test
    @DisplayName("getAll: genres ordered by id")
    void getAll_genresOrderedById() {
        List<Genre> genres = this.genreStorage.getAll();

        assertNotNull(genres);
        assertEquals(1, genres.get(0).getId());
        assertEquals(2, genres.get(1).getId());
        assertEquals(3, genres.get(2).getId());
        assertEquals(4, genres.get(3).getId());
        assertEquals(5, genres.get(4).getId());
        assertEquals(6, genres.get(5).getId());
    }

    @Test
    @DisplayName("getAll: return correct names")
    void getAll_returnCorrectNames() {
        List<Genre> genres = this.genreStorage.getAll();

        assertEquals("Комедия", genres.get(0).getName());
        assertEquals("Драма", genres.get(1).getName());
        assertEquals("Мультфильм", genres.get(2).getName());
        assertEquals("Триллер", genres.get(3).getName());
        assertEquals("Документальный", genres.get(4).getName());
        assertEquals("Боевик", genres.get(5).getName());
    }

    @Test
    @DisplayName("getGenreById: genre exists -> return genre")
    void getGenreById_genreExists_returnGenre() {
        Optional<Genre> genre = this.genreStorage.getGenreById(1);

        assertTrue(genre.isPresent());
        assertEquals(1, genre.get().getId());
        assertEquals("Комедия", genre.get().getName());
    }

    @Test
    @DisplayName("getGenreById: genre Drama -> return Drama")
    void getGenreById_genreDrama_returnDrama() {
        Optional<Genre> genre = this.genreStorage.getGenreById(2);

        assertTrue(genre.isPresent());
        assertEquals(2, genre.get().getId());
        assertEquals("Драма", genre.get().getName());
    }

    @Test
    @DisplayName("getGenreById: genre Cartoon -> return Cartoon")
    void getGenreById_genreCartoon_returnCartoon() {
        Optional<Genre> genre = this.genreStorage.getGenreById(3);

        assertTrue(genre.isPresent());
        assertEquals(3, genre.get().getId());
        assertEquals("Мультфильм", genre.get().getName());
    }

    @Test
    @DisplayName("getGenreById: genre Thriller -> return Thriller")
    void getGenreById_genreThriller_returnThriller() {
        Optional<Genre> genre = this.genreStorage.getGenreById(4);

        assertTrue(genre.isPresent());
        assertEquals(4, genre.get().getId());
        assertEquals("Триллер", genre.get().getName());
    }

    @Test
    @DisplayName("getGenreById: genre Documentary -> return Documentary")
    void getGenreById_genreDocumentary_returnDocumentary() {
        Optional<Genre> genre = this.genreStorage.getGenreById(5);

        assertTrue(genre.isPresent());
        assertEquals(5, genre.get().getId());
        assertEquals("Документальный", genre.get().getName());
    }

    @Test
    @DisplayName("getGenreById: genre Action -> return Action")
    void getGenreById_genreAction_returnAction() {
        Optional<Genre> genre = this.genreStorage.getGenreById(6);

        assertTrue(genre.isPresent());
        assertEquals(6, genre.get().getId());
        assertEquals("Боевик", genre.get().getName());
    }

    @Test
    @DisplayName("getGenreById: genre does not exist -> return empty")
    void getGenreById_genreDoesNotExist_returnEmpty() {
        Optional<Genre> genre = this.genreStorage.getGenreById(999);

        assertFalse(genre.isPresent());
    }

    @Test
    @DisplayName("getAll: list not empty")
    void getAll_listNotEmpty() {
        List<Genre> genres = this.genreStorage.getAll();

        assertFalse(genres.isEmpty());
    }

    @Test
    @DisplayName("getGenreById: negative id -> return empty")
    void getGenreById_negativeId_returnEmpty() {
        Optional<Genre> genre = this.genreStorage.getGenreById(-1);

        assertFalse(genre.isPresent());
    }
}

