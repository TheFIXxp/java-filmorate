package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private static final String INSERT_DIRECTOR = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_DIRECTOR = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String SELECT_DIRECTOR_BY_ID = "SELECT id, name FROM directors WHERE id = ?";
    private static final String SELECT_ALL_DIRECTORS = "SELECT id, name FROM directors";
    private static final String DELETE_DIRECTOR = "DELETE FROM directors WHERE id = ?";
    private static final String SELECT_DIRECTORS_BY_FILMS =
            "SELECT fd.film_id, d.id, d.name " +
                    "FROM directors d " +
                    "JOIN film_directors fd ON d.id = fd.director_id " +
                    "WHERE fd.film_id IN (%s)";

    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;


    @Override
    public List<Director> getAll() {
        log.info("DB request: get all directors");
        return this.jdbcTemplate.query(SELECT_ALL_DIRECTORS, this.directorRowMapper);
    }

    @Override
    public Director create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_DIRECTOR, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Director added: {}", director);
        return director;
    }

    @Override
    public Director update(Director director) {
        this.jdbcTemplate.update(UPDATE_DIRECTOR, director.getName(), director.getId());
        log.info("Director updated: {}", director);
        return director;
    }

    @Override
    public Optional<Director> getById(Long directorId) {
        return this.jdbcTemplate.query(SELECT_DIRECTOR_BY_ID, this.directorRowMapper, directorId)
                .stream()
                .findFirst();
    }

    @Override
    public void delete(Long directorId) {
        this.jdbcTemplate.update(DELETE_DIRECTOR, directorId);
        log.info("Director with id {} has been deleted", directorId);
    }

    @Override
    public Map<Long, Set<Director>> getDirectorsForFilms(Collection<Long> filmIds) {
        Map<Long, Set<Director>> directorsByFilmId = new HashMap<>();
        if (filmIds == null || filmIds.isEmpty()) {
            return directorsByFilmId;
        }

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = String.format(SELECT_DIRECTORS_BY_FILMS, inSql);

        this.jdbcTemplate.query(sql, rs -> {
            long filmId = rs.getLong("film_id");
            Director director = directorRowMapper.mapRow(rs, rs.getRow());

            directorsByFilmId.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
        }, filmIds.toArray());

        return directorsByFilmId;
    }
}
