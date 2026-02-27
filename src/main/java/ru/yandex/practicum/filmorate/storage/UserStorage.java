package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(long userId);

    Collection<User> getUsers();

    void addFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    Collection<User> getFriends(long userId);
}
