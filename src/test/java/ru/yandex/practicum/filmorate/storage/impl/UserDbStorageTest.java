package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("addUser: new user -> user id generated")
    void addUser_newUser_idGenerated() {
        User user = TestDataFactory.createValidUser();

        User addedUser = this.userStorage.addUser(user);

        assertNotNull(addedUser);
        assertNotEquals(0, addedUser.getId());
        assertEquals("user@example.com", addedUser.getEmail());
        assertEquals("login", addedUser.getLogin());
        assertEquals("Name", addedUser.getName());
    }

    @Test
    @DisplayName("addUser: multiple users -> unique ids generated")
    void addUser_multipleUsers_uniqueIdsGenerated() {
        User user1 = TestDataFactory.createValidUser();
        User user2 = TestDataFactory.createValidUser2();

        User added1 = this.userStorage.addUser(user1);
        User added2 = this.userStorage.addUser(user2);

        assertNotEquals(added1.getId(), added2.getId());
        assertTrue(added1.getId() > 0);
        assertTrue(added2.getId() > 0);
    }

    @Test
    @DisplayName("updateUser: existing user -> user updated")
    void updateUser_existingUser_userUpdated() {
        User originalUser = TestDataFactory.createValidUser();
        User addedUser = this.userStorage.addUser(originalUser);
        long userId = addedUser.getId();

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updatedlogin");
        updatedUser.setName("Updated User");
        updatedUser.setBirthday(originalUser.getBirthday());

        User result = this.userStorage.updateUser(updatedUser);

        assertEquals(userId, result.getId());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("updatedlogin", result.getLogin());
        assertEquals("Updated User", result.getName());
    }

    @Test
    @DisplayName("getUserById: user exists -> return user")
    void getUserById_userExists_returnUser() {
        User user = TestDataFactory.createValidUser();
        User addedUser = this.userStorage.addUser(user);
        long userId = addedUser.getId();

        Optional<User> foundUser = this.userStorage.getUserById(userId);

        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        assertEquals("user@example.com", foundUser.get().getEmail());
        assertEquals("login", foundUser.get().getLogin());
    }

    @Test
    @DisplayName("getUserById: user does not exist -> return empty")
    void getUserById_userDoesNotExist_returnEmpty() {
        Optional<User> foundUser = this.userStorage.getUserById(9999);

        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("getUsers: users exist -> return all users")
    void getUsers_usersExist_returnAllUsers() {
        User user1 = TestDataFactory.createValidUser();
        User user2 = TestDataFactory.createValidUser2();
        this.userStorage.addUser(user1);
        this.userStorage.addUser(user2);

        Collection<User> users = this.userStorage.getUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("getUsers: no users -> return empty collection")
    void getUsers_noUsers_returnEmptyCollection() {
        Collection<User> users = this.userStorage.getUsers();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("addFriend: two users exist -> friend added")
    void addFriend_twoUsersExist_friendAdded() {
        User user1 = TestDataFactory.createValidUser();
        User user2 = TestDataFactory.createValidUser2();
        User addedUser1 = this.userStorage.addUser(user1);
        User addedUser2 = this.userStorage.addUser(user2);

        this.userStorage.addFriend(addedUser1.getId(), addedUser2.getId());

        Collection<User> friends = this.userStorage.getFriends(addedUser1.getId());

        assertEquals(1, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId() == addedUser2.getId()));
    }

    @Test
    @DisplayName("addFriend: same friend twice -> no duplicates")
    void addFriend_sameFriendTwice_noDuplicates() {
        User user1 = TestDataFactory.createValidUser();
        User user2 = TestDataFactory.createValidUser2();
        User addedUser1 = this.userStorage.addUser(user1);
        User addedUser2 = this.userStorage.addUser(user2);

        this.userStorage.addFriend(addedUser1.getId(), addedUser2.getId());
        this.userStorage.addFriend(addedUser1.getId(), addedUser2.getId());

        Collection<User> friends = this.userStorage.getFriends(addedUser1.getId());

        assertEquals(1, friends.size());
    }

    @Test
    @DisplayName("removeFriend: friend exists -> friend removed")
    void removeFriend_friendExists_friendRemoved() {
        User user1 = TestDataFactory.createValidUser();
        User user2 = TestDataFactory.createValidUser2();
        User addedUser1 = this.userStorage.addUser(user1);
        User addedUser2 = this.userStorage.addUser(user2);

        this.userStorage.addFriend(addedUser1.getId(), addedUser2.getId());
        this.userStorage.removeFriend(addedUser1.getId(), addedUser2.getId());

        Collection<User> friends = this.userStorage.getFriends(addedUser1.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    @DisplayName("getFriends: user has friends -> return all friends")
    void getFriends_userHasFriends_returnAllFriends() {
        User user1 = TestDataFactory.createValidUser();
        User user2 = TestDataFactory.createValidUser2();
        User user3 = TestDataFactory.createValidUser3();
        User addedUser1 = this.userStorage.addUser(user1);
        User addedUser2 = this.userStorage.addUser(user2);
        User addedUser3 = this.userStorage.addUser(user3);

        this.userStorage.addFriend(addedUser1.getId(), addedUser2.getId());
        this.userStorage.addFriend(addedUser1.getId(), addedUser3.getId());

        Collection<User> friends = this.userStorage.getFriends(addedUser1.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId() == addedUser2.getId()));
        assertTrue(friends.stream().anyMatch(u -> u.getId() == addedUser3.getId()));
    }

    @Test
    @DisplayName("getFriends: user has no friends -> return empty collection")
    void getFriends_userHasNoFriends_returnEmptyCollection() {
        User user = TestDataFactory.createValidUser();
        User addedUser = this.userStorage.addUser(user);

        Collection<User> friends = this.userStorage.getFriends(addedUser.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    @DisplayName("addUser: user with null name -> return user with null name")
    void addUser_userWithNullName_returnUserWithNullName() {
        User user = TestDataFactory.createValidUser();
        user.setName(null);

        User addedUser = this.userStorage.addUser(user);

        Optional<User> foundUser = this.userStorage.getUserById(addedUser.getId());

        assertTrue(foundUser.isPresent());
        assertNull(foundUser.get().getName());
    }
}

