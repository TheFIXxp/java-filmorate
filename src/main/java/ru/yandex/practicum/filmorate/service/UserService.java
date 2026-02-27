package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService {

    final UserStorage userStorage;

    public User addUser(User user) {
        applyDefaultName(user);
        User stored = this.userStorage.addUser(user);
        log.info("User added: id={}, login={}", stored.getId(), stored.getLogin());
        return stored;
    }

    public User updateUser(User user) {
        applyDefaultName(user);
        this.userStorage.getUserById(user.getId())
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(user.getId())));
        User stored = this.userStorage.updateUser(user);
        log.info("User updated: id={}, login={}", stored.getId(), stored.getLogin());
        return stored;
    }

    private void applyDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public Collection<User> getUsers() {
        log.info("Get all users");
        return this.userStorage.getUsers();
    }

    public User getUserById(long userId) {
        log.info("Get user by id={}", userId);
        return this.userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));
    }

    public void addFriend(long userId, long friendId) {
        ensureDifferentUsers(userId, friendId);
        ensureUserExists(userId);
        ensureUserExists(friendId);
        this.userStorage.addFriend(userId, friendId);
        log.info("Friend added: userId={}, friendId={}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        ensureDifferentUsers(userId, friendId);
        ensureUserExists(userId);
        ensureUserExists(friendId);
        this.userStorage.removeFriend(userId, friendId);
        log.info("Friend removed: userId={}, friendId={}", userId, friendId);
    }

    public Collection<User> getFriends(long userId) {
        ensureUserExists(userId);
        log.info("Get friends: userId={}", userId);
        return this.userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(long userId, long otherId) {
        ensureDifferentUsers(userId, otherId);
        ensureUserExists(userId);
        ensureUserExists(otherId);
        log.info("Get common friends: userId={}, otherId={}", userId, otherId);

        Collection<User> firstFriends = this.userStorage.getFriends(userId);
        Collection<User> secondFriends = this.userStorage.getFriends(otherId);

        if (firstFriends == null || secondFriends == null) {
            return Collections.emptyList();
        }

        Set<Long> secondFriendsIds = secondFriends.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return firstFriends.stream()
                .filter(user -> secondFriendsIds.contains(user.getId()))
                .collect(Collectors.toList());
    }

    private void ensureUserExists(long userId) {
        this.userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));
    }

    private void ensureDifferentUsers(long userId, long otherId) {
        if (userId == otherId) {
            throw new ValidationException("userId и otherId должны ссылаться на разных пользователей");
        }
    }
}
