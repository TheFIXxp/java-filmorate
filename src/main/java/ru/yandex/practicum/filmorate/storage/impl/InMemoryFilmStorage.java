package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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
        if (!this.films.containsKey(film.getId())) {
            throw new NoSuchElementException("Film with id %s not found".formatted(film.getId()));
        }
        this.films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film getFilmById(long filmId) {
        Film film = this.films.get(filmId);
        if (film == null) {
            throw new NoSuchElementException("Film with id %s not found".formatted(filmId));
        }
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        return this.films.values();
    }

    @Override
    public void addLike(long filmId, long userId) {
        ensureFilmExists(filmId);
        this.likesByFilmId
                .computeIfAbsent(filmId, ignored -> new HashSet<>())
                .add(userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        ensureFilmExists(filmId);
        Set<Long> likes = this.likesByFilmId.get(filmId);
        if (likes != null) {
            likes.remove(userId);
        }
    }

    @Override
    public Collection<Film> getPopular(int count) {
        if (this.films.isEmpty()) {
            return Collections.emptyList();
        }
        List<Film> sorted = new ArrayList<>(this.films.values());
        sorted.sort(Comparator
                .comparingInt((Film film) -> likesCount(film.getId()))
                .reversed()
                .thenComparingLong(Film::getId));
        if (count >= sorted.size()) {
            return sorted;
        }
        return sorted.subList(0, count);
    }

    private int likesCount(long filmId) {
        return this.likesByFilmId.getOrDefault(filmId, Collections.emptySet()).size();
    }

    private void ensureFilmExists(long filmId) {
        if (!this.films.containsKey(filmId)) {
            throw new NoSuchElementException("Film with id %s not found".formatted(filmId));
        }
    }
}
