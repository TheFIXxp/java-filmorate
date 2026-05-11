package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;

public interface DirectorStorage {

    Optional<Director> getById(Long id);

    List<Director> getAll();

    Director create(Director director);

    Director update(Director director);

    void delete(Long directorId);

    Map<Long, Set<Director>> getDirectorsForFilms(Collection<Long> filmIds);
}
