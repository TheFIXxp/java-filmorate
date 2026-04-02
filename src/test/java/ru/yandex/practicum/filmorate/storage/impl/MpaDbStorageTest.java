package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    @DisplayName("getAll: return all mpa ratings")
    void getAll_returnAllMpaRatings() {
        List<Mpa> mpaList = this.mpaStorage.getAll();

        assertNotNull(mpaList);
        assertEquals(5, mpaList.size());
    }

    @Test
    @DisplayName("getAll: ratings ordered by id")
    void getAll_ratingsOrderedById() {
        List<Mpa> mpaList = this.mpaStorage.getAll();

        assertNotNull(mpaList);
        assertEquals(1, mpaList.get(0).getId());
        assertEquals(2, mpaList.get(1).getId());
        assertEquals(3, mpaList.get(2).getId());
        assertEquals(4, mpaList.get(3).getId());
        assertEquals(5, mpaList.get(4).getId());
    }

    @Test
    @DisplayName("getAll: return correct names")
    void getAll_returnCorrectNames() {
        List<Mpa> mpaList = this.mpaStorage.getAll();

        assertEquals("G", mpaList.get(0).getName());
        assertEquals("PG", mpaList.get(1).getName());
        assertEquals("PG-13", mpaList.get(2).getName());
        assertEquals("R", mpaList.get(3).getName());
        assertEquals("NC-17", mpaList.get(4).getName());
    }

    @Test
    @DisplayName("getById: rating exists -> return rating")
    void getMpaById_ratingExists_returnRating() {
        Optional<Mpa> mpa = this.mpaStorage.getMpaById(1);

        assertTrue(mpa.isPresent());
        assertEquals(1, mpa.get().getId());
        assertEquals("G", mpa.get().getName());
    }

    @Test
    @DisplayName("getById: rating PG -> return PG")
    void getMpaById_ratingPG_returnPG() {
        Optional<Mpa> mpa = this.mpaStorage.getMpaById(2);

        assertTrue(mpa.isPresent());
        assertEquals(2, mpa.get().getId());
        assertEquals("PG", mpa.get().getName());
    }

    @Test
    @DisplayName("getById: rating PG-13 -> return PG-13")
    void getMpaById_ratingPG13_returnPG13() {
        Optional<Mpa> mpa = this.mpaStorage.getMpaById(3);

        assertTrue(mpa.isPresent());
        assertEquals(3, mpa.get().getId());
        assertEquals("PG-13", mpa.get().getName());
    }

    @Test
    @DisplayName("getById: rating R -> return R")
    void getMpaById_ratingR_returnR() {
        Optional<Mpa> mpa = this.mpaStorage.getMpaById(4);

        assertTrue(mpa.isPresent());
        assertEquals(4, mpa.get().getId());
        assertEquals("R", mpa.get().getName());
    }

    @Test
    @DisplayName("getById: rating NC-17 -> return NC-17")
    void getMpaById_ratingNC17_returnNC17() {
        Optional<Mpa> mpa = this.mpaStorage.getMpaById(5);

        assertTrue(mpa.isPresent());
        assertEquals(5, mpa.get().getId());
        assertEquals("NC-17", mpa.get().getName());
    }

    @Test
    @DisplayName("getById: rating does not exist -> return empty")
    void getMpaById_ratingDoesNotExist_returnEmpty() {
        Optional<Mpa> mpa = this.mpaStorage.getMpaById(999);

        assertFalse(mpa.isPresent());
    }

    @Test
    @DisplayName("getAll: list not empty")
    void getAll_listNotEmpty() {
        List<Mpa> mpaList = this.mpaStorage.getAll();

        assertFalse(mpaList.isEmpty());
    }
}

