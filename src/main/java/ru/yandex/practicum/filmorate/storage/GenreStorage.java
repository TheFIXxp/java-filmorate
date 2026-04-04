package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

public interface GenreStorage {

    List<Genre> getAll();

    Optional<Genre> getGenreById(int id);

    Map<Long, Set<Genre>> getGenresForFilms(Collection<Long> filmIds);

    List<Genre> getGenresByIds(Collection<Integer> genreIds);

}