package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private static final String INSERT_USER = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String INSERT_FRIENDSHIP = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";

    private static final String UPDATE_USER = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    private static final String DELETE_FRIENDSHIP = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";

    private static final String SELECT_USER_BY_ID = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";

    private static final String SELECT_ALL_USERS = "SELECT id, email, login, name, birthday FROM users";

    private static final String SELECT_FRIENDS = "SELECT u.id, u.email, u.login, u.name, u.birthday " + "FROM users u " + "INNER JOIN friendships f ON u.id = f.friend_id " + "WHERE f.user_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        this.jdbcTemplate.update(UPDATE_USER, user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()), user.getId());
        return user;
    }

    @Override
    public Optional<User> getUserById(long userId) {
        return this.jdbcTemplate.query(SELECT_USER_BY_ID, this.userRowMapper::mapRow, userId).stream().findFirst();
    }

    @Override
    public Collection<User> getUsers() {
        return this.jdbcTemplate.query(SELECT_ALL_USERS, this.userRowMapper);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        try {
            this.jdbcTemplate.update(INSERT_FRIENDSHIP, userId, friendId);
        } catch (Exception e) {
            log.debug("Friendship already exists: {} -> {}", userId, friendId);
        }
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        this.jdbcTemplate.update(DELETE_FRIENDSHIP, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(long userId) {
        return this.jdbcTemplate.query(SELECT_FRIENDS, this.userRowMapper::mapRow, userId);
    }
}
