package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService {

    final Map<Integer, User> users = new HashMap<>();
    int nextId = 1;

    public User addUser(User user) {
        applyDefaultName(user);
        user.setId(this.nextId++);
        this.users.put(user.getId(), user);
        log.info("User added: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    public User updateUser(User user) {
        if (!this.users.containsKey(user.getId())) {
            log.warn("User update failed: id={} not found", user.getId());
            throw new NoSuchElementException("User with id %s not found".formatted(user.getId()));
        }
        applyDefaultName(user);
        this.users.put(user.getId(), user);
        log.info("User updated: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    private void applyDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public Collection<User> getUsers() {
        log.info("Get all users");
        return this.users.values();
    }
}
