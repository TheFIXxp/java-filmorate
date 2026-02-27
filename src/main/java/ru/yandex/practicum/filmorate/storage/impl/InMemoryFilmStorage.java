package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likesByFilmId = new HashMap<>();
    private long nextId = 1;

    @Override
    public Film addFilm(Film film) {
        film.setId(this.nextId++);
        this.films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        this.films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> getFilmById(long filmId) {
        return Optional.ofNullable(this.films.get(filmId));
    }

    @Override
    public Collection<Film> getFilms() {
        return this.films.values();
    }

    @Override
    public void addLike(long filmId, long userId) {
        this.likesByFilmId
                .computeIfAbsent(filmId, ignored -> new HashSet<>())
                .add(userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        Set<Long> likes = this.likesByFilmId.get(filmId);
        if (likes != null) {
            likes.remove(userId);
        }
    }

    @Override
    public int getLikesCount(long filmId) {
        return this.likesByFilmId.getOrDefault(filmId, Collections.emptySet()).size();
    }

}
