package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Set<Long>> friendsByUserId = new HashMap<>();
    private long nextId = 1;

    @Override
    public User addUser(User user) {
        user.setId(this.nextId++);
        this.users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!this.users.containsKey(user.getId())) {
            throw new NoSuchElementException("User with id %s not found".formatted(user.getId()));
        }
        this.users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(long userId) {
        User user = this.users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("User with id %s not found".formatted(userId));
        }
        return user;
    }

    @Override
    public Collection<User> getUsers() {
        return this.users.values();
    }

    @Override
    public void addFriend(long userId, long friendId) {
        ensureUserExists(userId);
        ensureUserExists(friendId);
        this.friendsByUserId
                .computeIfAbsent(userId, ignored -> new HashSet<>())
                .add(friendId);
        this.friendsByUserId
                .computeIfAbsent(friendId, ignored -> new HashSet<>())
                .add(userId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        ensureUserExists(userId);
        ensureUserExists(friendId);
        Set<Long> userFriends = this.friendsByUserId.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }
        Set<Long> friendFriends = this.friendsByUserId.get(friendId);
        if (friendFriends != null) {
            friendFriends.remove(userId);
        }
    }

    @Override
    public Collection<User> getFriends(long userId) {
        ensureUserExists(userId);
        return this.friendsByUserId.getOrDefault(userId, Collections.emptySet())
                .stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(long userId, long otherId) {
        ensureUserExists(userId);
        ensureUserExists(otherId);
        Set<Long> first = this.friendsByUserId.getOrDefault(userId, Collections.emptySet());
        Set<Long> second = this.friendsByUserId.getOrDefault(otherId, Collections.emptySet());
        return first.stream()
                .filter(second::contains)
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    private void ensureUserExists(long userId) {
        if (!this.users.containsKey(userId)) {
            throw new NoSuchElementException("User with id %s not found".formatted(userId));
        }
    }
}
