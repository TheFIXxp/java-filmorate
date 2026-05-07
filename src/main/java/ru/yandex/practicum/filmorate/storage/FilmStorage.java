package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(long id);

    Optional<Film> getFilmById(long filmId);

    Collection<Film> getFilms();

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    int getLikesCount(long filmId);

    Collection<Film> getPopularFilms(int count);

    Collection<Film> getFilmsByDirector(long directorId, String sortBy);

    Collection<Film> getPopularFilms(int count, Integer genreId, Integer year);

    Collection<Film> getCommonFilms(long userId, long friendId);
}
