package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(long filmId);

    Collection<Film> getFilms();

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    Collection<Film> getPopular(int count);
}
