package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MpaService {

    MpaStorage mpaStorage;

    public Collection<Mpa> getAllMpa() {
        log.info("Get all mpa request");
        return this.mpaStorage.getAll();
    }

    public Mpa getMpa(int id) {
        log.info("Get mpa request, id: {}", id);
        return this.mpaStorage.getMpaById(id).orElseThrow(() -> new NotFoundException("MPA not found"));
    }

}
