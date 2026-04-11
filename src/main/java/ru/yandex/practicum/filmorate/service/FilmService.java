package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    final FilmStorage filmStorage;
    final UserStorage userStorage;
    final GenreStorage genreStorage;
    final MpaService mpaService;
    final GenreService genreService;

    public Film addFilm(Film film) {
        validateDate(film);
        validateMpa(film);
        validateGenres(film);
        Film stored = this.filmStorage.addFilm(film);
        log.info("Film added: id={}, name={}", stored.getId(), stored.getName());
        return stored;
    }

    public Film updateFilm(Film film) {
        ensureFilmExists(film.getId());
        validateDate(film);
        validateMpa(film);
        validateGenres(film);
        Film stored = this.filmStorage.updateFilm(film);
        log.info("Film updated: id={}, name={}", stored.getId(), stored.getName());
        return film;
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

    private void validateMpa(Film film) {
        if (film.getMpa() == null) {
            log.warn("Film validation failed: MPA is required");
            throw new ValidationException("MPA is required");
        }
        this.mpaService.getMpa(film.getMpa().getId());
    }

    private void validateGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Collection<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());

            this.genreService.getGenres(genreIds);
        }
    }

    public Collection<Film> getFilms() {
        log.info("Get all films");
        Collection<Film> films = this.filmStorage.getFilms();
        enrichFilmsWithGenres(films);
        return films;
    }

    public Film getFilmById(long filmId) {
        log.info("Get film by id={}", filmId);
        Film film = this.filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id %s not found".formatted(filmId)));

        Map<Long, Set<Genre>> longSetMap = this.genreStorage.getGenresForFilms(List.of(filmId));
        film.setGenres(longSetMap.getOrDefault(filmId, Set.of()));

        return film;
    }

    public void addLike(long filmId, long userId) {
        ensureFilmExists(filmId);
        ensureUserExists(userId);
        this.filmStorage.addLike(filmId, userId);
        log.info("Like added: filmId={}, userId={}", filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        ensureFilmExists(filmId);
        ensureUserExists(userId);
        this.filmStorage.removeLike(filmId, userId);
        log.info("Like removed: filmId={}, userId={}", filmId, userId);
    }

    public void ensureFilmExists(long filmId) {
        this.filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id %s not found".formatted(filmId)));
    }

    private void ensureUserExists(long userId) {
        this.userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));
    }

    public Collection<Film> getPopular(int count) {
        log.info("Get popular films: count={}", count);
        Collection<Film> films = this.filmStorage.getPopularFilms(count);
        enrichFilmsWithGenres(films);
        return films;
    }

    private void enrichFilmsWithGenres(Collection<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        Collection<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Long, Set<Genre>> genresByFilmId = this.genreStorage.getGenresForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), Set.of()));
        }
    }
}
