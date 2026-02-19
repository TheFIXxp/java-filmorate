package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    final FilmStorage filmStorage;
    final UserStorage userStorage;

    public Film addFilm(Film film) {
        validateDate(film);
        Film stored = this.filmStorage.addFilm(film);
        log.info("Film added: id={}, name={}", stored.getId(), stored.getName());
        return stored;
    }

    public Film updateFilm(Film film) {
        validateDate(film);
        Film stored = this.filmStorage.updateFilm(film);
        log.info("Film updated: id={}, name={}", stored.getId(), stored.getName());
        return stored;
    }

    private void validateDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn(
                    "Film validation failed: releaseDate={} is before {}",
                    film.getReleaseDate(),
                    MIN_RELEASE_DATE
            );
            throw new ValidationException("Release date cannot be before %s".formatted(MIN_RELEASE_DATE));
        }
    }

    public Collection<Film> getFilms() {
        log.info("Get all films");
        return this.filmStorage.getFilms();
    }

    public Film getFilmById(long filmId) {
        log.info("Get film by id={}", filmId);
        return this.filmStorage.getFilmById(filmId);
    }

    public void addLike(long filmId, long userId) {
        this.userStorage.getUserById(userId);
        this.filmStorage.addLike(filmId, userId);
        log.info("Like added: filmId={}, userId={}", filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        this.userStorage.getUserById(userId);
        this.filmStorage.removeLike(filmId, userId);
        log.info("Like removed: filmId={}, userId={}", filmId, userId);
    }

    public Collection<Film> getPopular(int count) {
        log.info("Get popular films: count={}", count);
        return this.filmStorage.getPopular(count);
    }
}
