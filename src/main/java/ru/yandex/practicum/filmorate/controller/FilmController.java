package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/films")
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
    public Iterable<Film> getPopular(@Valid @RequestParam(defaultValue = "10") @Positive int count) {
        return this.filmService.getPopular(count);
    }
}
