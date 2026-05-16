package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/films")
@Validated
public class FilmController {

    FilmService filmService;

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return this.filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return this.filmService.updateFilm(film);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable long id) {
        filmService.deleteFilm(id);
    }

    @GetMapping
    public Iterable<Film> getFilms() {
        return this.filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable long id) {
        return this.filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        this.filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        this.filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(
            @RequestParam(defaultValue = "10") @Positive int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        return this.filmService.getPopular(count, genreId, year);
    }

    @GetMapping("/common")
    public Collection<Film> getCommon(@RequestParam @Positive long userId, @RequestParam @Positive long friendId) {
        return this.filmService.getCommon(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public Iterable<Film> getFilmsByDirector(
            @PathVariable Long directorId,
            @RequestParam(defaultValue = "likes") String sortBy
    ) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(@RequestParam String query, @RequestParam String by) {
        Set<String> filters = parseSearchBy(by);
        boolean byTitle = filters.contains("title");
        boolean byDirector = filters.contains("director");
        return filmService.searchFilms(query, byTitle, byDirector);
    }

    private Set<String> parseSearchBy(String by) {
        if (by == null || by.isBlank()) {
            throw new ValidationException("by must contain director and/or title");
        }

        Set<String> filters = java.util.Arrays.stream(by.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (filters.isEmpty() || !filters.stream().allMatch(value -> value.equals("title") || value.equals("director"))) {
            throw new ValidationException("by must contain director and/or title");
        }

        return filters;
    }
}
