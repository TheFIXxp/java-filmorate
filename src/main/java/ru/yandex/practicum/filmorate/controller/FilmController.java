package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
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
}
