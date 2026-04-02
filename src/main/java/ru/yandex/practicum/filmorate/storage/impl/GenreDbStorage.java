package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private static final String SELECT_ALL_GENRES = "SELECT id, name FROM genres ORDER BY id";

    private static final String SELECT_GENRE_BY_ID = "SELECT id, name FROM genres WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    @Override
    public List<Genre> getAll() {
        return this.jdbcTemplate.query(SELECT_ALL_GENRES, this.genreRowMapper);
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        return this.jdbcTemplate.query(SELECT_GENRE_BY_ID, this.genreRowMapper, id).stream().findFirst();
    }
}
