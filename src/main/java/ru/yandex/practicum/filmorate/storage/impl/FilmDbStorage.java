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

    private static final String SELECT_COMMON_FILMS = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " + "f.mpa_id, m.id as mpa_id_from_join, m.name as mpa_name " + "FROM films f " + "LEFT JOIN mpa m ON f.mpa_id = m.id " + "WHERE f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?) " + "AND f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?) " + "ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_id = f.id) DESC, f.id ASC";

    private static final String SELECT_RECOMMENDED_FILMS = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " + "f.mpa_id, m.id as mpa_id_from_join, m.name as mpa_name " + "FROM films f " + "LEFT JOIN mpa m ON f.mpa_id = m.id " + "WHERE f.id IN (" +
            "SELECT fl_recommend.film_id " +
            "FROM film_likes fl_recommend " +
            "WHERE fl_recommend.user_id = (" +
            "    SELECT other_user_id FROM (" +
            "        SELECT fl_other.user_id AS other_user_id, COUNT(*) AS common_likes " +
            "        FROM film_likes fl_self " +
            "        JOIN film_likes fl_other ON fl_self.film_id = fl_other.film_id " +
            "        WHERE fl_self.user_id = ? AND fl_other.user_id <> ? " +
            "        GROUP BY fl_other.user_id " +
            "        ORDER BY common_likes DESC, other_user_id ASC " +
            "        LIMIT 1" +
            "    ) AS best_match" +
            ") " +
            "AND fl_recommend.film_id NOT IN (SELECT film_id FROM film_likes WHERE user_id = ?)" +
            ") " +
            "ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_id = f.id) DESC, f.id ASC";

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

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        this.jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

        this.jdbcTemplate.update(DELETE_FILM_GENRES, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenresToFilm(film);
        }

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
    public Collection<Film> getCommonFilms(long userId, long friendId) {
        return this.jdbcTemplate.query(SELECT_COMMON_FILMS, (rs, rowNum) -> {
            Film f = this.filmRowMapper.mapRow(rs, rowNum);
            loadMpaFromResultSet(rs, f);
            return f;
        }, userId, friendId);
    }

    @Override
    public Collection<Film> getRecommendations(long userId) {
        return this.jdbcTemplate.query(SELECT_RECOMMENDED_FILMS, (rs, rowNum) -> {
            Film f = this.filmRowMapper.mapRow(rs, rowNum);
            loadMpaFromResultSet(rs, f);
            return f;
        }, userId, userId, userId);
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


}
