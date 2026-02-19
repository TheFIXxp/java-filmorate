package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

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
        return this.userStorage.getUserById(userId);
    }

    public void addFriend(long userId, long friendId) {
        this.userStorage.addFriend(userId, friendId);
        log.info("Friend added: userId={}, friendId={}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        this.userStorage.removeFriend(userId, friendId);
        log.info("Friend removed: userId={}, friendId={}", userId, friendId);
    }

    public Collection<User> getFriends(long userId) {
        log.info("Get friends: userId={}", userId);
        return this.userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(long userId, long otherId) {
        log.info("Get common friends: userId={}, otherId={}", userId, otherId);
        return this.userStorage.getCommonFriends(userId, otherId);
    }
}
