package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    final Map<Integer, Film> films = new HashMap<>();
    int nextId = 1;

    public Film addFilm(Film film) {
        validateDate(film);
        film.setId(this.nextId++);
        this.films.put(film.getId(), film);
        log.info("Film added: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    public Film updateFilm(Film film) {
        if (!this.films.containsKey(film.getId())) {
            log.warn("Film update failed: id={} not found", film.getId());
            throw new NoSuchElementException("Film with id %s not found".formatted(film.getId()));
        }
        validateDate(film);

        this.films.put(film.getId(), film);
        log.info("Film updated: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    private static void validateDate(Film film) {
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
        return this.films.values();
    }
}
