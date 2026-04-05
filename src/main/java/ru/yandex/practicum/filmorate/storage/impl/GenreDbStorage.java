package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private static final String SELECT_ALL_GENRES = "SELECT id, name FROM genres ORDER BY id";

    private static final String SELECT_GENRE_BY_ID = "SELECT id, name FROM genres WHERE id = ?";

    private static final String SELECT_GENRES_FOR_FILMS = "SELECT fg.film_id, g.id, g.name FROM film_genres fg " + "INNER JOIN genres g ON fg.genre_id = g.id " + "WHERE fg.film_id IN (%s) " + "ORDER BY fg.film_id, g.id";

    private static final String SELECT_GENRES_BY_IDS = "SELECT id, name FROM genres WHERE id IN (%s) ORDER BY id";

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

    @Override
    public Map<Long, Set<Genre>> getGenresForFilms(Collection<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }

        String placeholders = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String query = SELECT_GENRES_FOR_FILMS.formatted(placeholders);

        return this.jdbcTemplate.query(query, (rs, rowNum) -> {
                    long filmId = rs.getLong("film_id");
                    Genre genre = this.genreRowMapper.mapRow(rs, rowNum);
                    if (genre != null) {
                        return Map.entry(filmId, genre);
                    }
                    return null;
                }, filmIds.toArray()).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(
                                Map.Entry::getValue,
                                Collectors.toCollection(HashSet::new)
                        )
                ));
    }

    @Override
    public List<Genre> getGenresByIds(Collection<Integer> genreIds) {
        if (genreIds.isEmpty()) {
            return List.of();
        }

        String placeholders = genreIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String query = SELECT_GENRES_BY_IDS.formatted(placeholders);

        return this.jdbcTemplate.query(query, this.genreRowMapper, genreIds.toArray());
    }
}


