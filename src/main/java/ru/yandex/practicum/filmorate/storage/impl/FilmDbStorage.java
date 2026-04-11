package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String INSERT_FILM = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

    private static final String INSERT_FILM_GENRE = "MERGE INTO film_genres KEY (film_id, genre_id) VALUES (?, ?)";

    private static final String INSERT_FILM_LIKE = "MERGE INTO film_likes KEY (film_id, user_id) VALUES (?, ?)";

    private static final String UPDATE_FILM = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";

    private static final String DELETE_FILM_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private static final String SELECT_FILM_WITH_MPA = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " + "f.mpa_id, m.id as mpa_id_from_join, m.name as mpa_name " + "FROM films f " + "LEFT JOIN mpa m ON f.mpa_id = m.id";

    private static final String SELECT_FILM_WITH_MPA_BY_ID = SELECT_FILM_WITH_MPA + " WHERE f.id = ?";

    private static final String SELECT_ALL_FILMS_WITH_MPA = SELECT_FILM_WITH_MPA + " ORDER BY f.id";

    private static final String SELECT_POPULAR_FILMS_WITH_MPA = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " + "f.mpa_id, m.id as mpa_id_from_join, m.name as mpa_name " + "FROM films f " + "LEFT JOIN mpa m ON f.mpa_id = m.id " + "LEFT JOIN film_likes fl ON f.id = fl.film_id " + "GROUP BY f.id " + "ORDER BY COUNT(fl.user_id) DESC, f.id ASC " + "LIMIT ?";

    private static final String SELECT_LIKE_COUNT = "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";

    private static final String INSERT_FILM_DIRECTOR = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS = "DELETE FROM film_directors WHERE film_id = ?";

    private static final String SELECT_BY_DIRECTOR_SORT_YEAR =
            SELECT_FILM_WITH_MPA +
                    " JOIN film_directors fd ON f.id = fd.film_id " +
                    " WHERE fd.director_id = ? ORDER BY f.release_date";

    private static final String SELECT_BY_DIRECTOR_SORT_LIKES =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                    "m.id as mpa_id_from_join, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.id, m.id " +
                    "ORDER BY COUNT(fl.user_id) DESC";

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenresToFilm(film);
        }

        addDirectorsToFilm(film);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        this.jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

        this.jdbcTemplate.update(DELETE_FILM_GENRES, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenresToFilm(film);
        }

        deleteDirectorsFromFilm(film.getId());
        addDirectorsToFilm(film);

        return film;
    }

    @Override
    public Optional<Film> getFilmById(long filmId) {
        return this.jdbcTemplate.query(SELECT_FILM_WITH_MPA_BY_ID, (rs, rowNum) -> {
            Film f = this.filmRowMapper.mapRow(rs, rowNum);
            loadMpaFromResultSet(rs, f);
            return f;
        }, filmId).stream().findFirst();
    }

    @Override
    public Collection<Film> getFilms() {
        return this.jdbcTemplate.query(SELECT_ALL_FILMS_WITH_MPA, (rs, rowNum) -> {
            Film f = this.filmRowMapper.mapRow(rs, rowNum);
            loadMpaFromResultSet(rs, f);
            return f;
        });
    }

    @Override
    public void addLike(long filmId, long userId) {
        this.jdbcTemplate.update(INSERT_FILM_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        this.jdbcTemplate.update(DELETE_FILM_LIKE, filmId, userId);
    }

    @Override
    public int getLikesCount(long filmId) {
        return this.jdbcTemplate.queryForObject(SELECT_LIKE_COUNT, Integer.class, filmId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        return this.jdbcTemplate.query(SELECT_POPULAR_FILMS_WITH_MPA, (rs, rowNum) -> {
            Film f = this.filmRowMapper.mapRow(rs, rowNum);
            loadMpaFromResultSet(rs, f);
            return f;
        }, count);
    }

    @Override
    public Collection<Film> getFilmsByDirector(long directorId, String sortBy) {
        String sql = sortBy.equals("year") ? SELECT_BY_DIRECTOR_SORT_YEAR : SELECT_BY_DIRECTOR_SORT_LIKES;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film f = this.filmRowMapper.mapRow(rs, rowNum);
            loadMpaFromResultSet(rs, f);
            return f;
        }, directorId);
    }

    private void loadMpaFromResultSet(java.sql.ResultSet rs, Film film) throws java.sql.SQLException {
        if (rs.getObject("mpa_id_from_join") != null) {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id_from_join"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }
    }

    private void addGenresToFilm(Film film) {
        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());

        this.jdbcTemplate.batchUpdate(INSERT_FILM_GENRE, batchArgs);
    }

    private void addDirectorsToFilm(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = film.getDirectors().stream()
                .map(director -> new Object[]{film.getId(), director.getId()})
                .collect(Collectors.toList());

        this.jdbcTemplate.batchUpdate(INSERT_FILM_DIRECTOR, batchArgs);
    }

    private void deleteDirectorsFromFilm(long filmId) {
        this.jdbcTemplate.update(DELETE_FILM_DIRECTORS, filmId);
    }
}
