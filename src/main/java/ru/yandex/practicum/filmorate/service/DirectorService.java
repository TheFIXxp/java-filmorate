package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.impl.DirectorDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectorService {

    DirectorDbStorage directorStorage;

    public List<Director> getAllDirectors() {
        log.info("Get all directors");
        return directorStorage.getAll();
    }

    public Director getDirectorById(Long id) {
        log.info("Get director by id={}", id);
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Director with id: " + id + " not found"));
    }

    public Director createDirector(Director director) {
        log.info("Create director {}", director.toString());
        return directorStorage.create(director);
    }

    public Director updateDirector(Director director) {
        log.info("Update director with id={}", director.getId());
        getDirectorById(director.getId());
        return directorStorage.update(director);
    }

    public void deleteDirector(Long id) {
        log.info("Delete director with id={}", id);
        getDirectorById(id);
        directorStorage.delete(id);
    }
}
