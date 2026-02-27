package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.impl.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private UserService userService;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        this.userStorage = new InMemoryUserStorage();
        this.userService = new UserService(this.userStorage);
    }

    @Test
    @DisplayName("getUserById: user does not exist -> throw NotFoundException")
    void getUserById_userDoesNotExist_throwNotFoundException() {
        assertThrows(NotFoundException.class, () -> this.userService.getUserById(999L));
    }

    @Test
    @DisplayName("updateUser: user does not exist -> throw NotFoundException")
    void updateUser_userDoesNotExist_throwNotFoundException() {
        User user = TestDataFactory.createValidUser();
        user.setId(999L);
        assertThrows(NotFoundException.class, () -> this.userService.updateUser(user));
    }

    @Test
    @DisplayName("addFriend: either user does not exist -> throw NotFoundException")
    void addFriend_eitherUserDoesNotExist_throwNotFoundException() {
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());

        assertThrows(NotFoundException.class, () -> this.userService.addFriend(user.getId(), 999L));
        assertThrows(NotFoundException.class, () -> this.userService.addFriend(999L, user.getId()));
    }

    @Test
    @DisplayName("removeFriend: either user does not exist -> throw NotFoundException")
    void removeFriend_eitherUserDoesNotExist_throwNotFoundException() {
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());

        assertThrows(NotFoundException.class, () -> this.userService.removeFriend(user.getId(), 999L));
        assertThrows(NotFoundException.class, () -> this.userService.removeFriend(999L, user.getId()));
    }

    @Test
    @DisplayName("getFriends: user does not exist -> throw NotFoundException")
    void getFriends_userDoesNotExist_throwNotFoundException() {
        assertThrows(NotFoundException.class, () -> this.userService.getFriends(999L));
    }

    @Test
    @DisplayName("getCommonFriends: either user does not exist -> throw NotFoundException")
    void getCommonFriends_eitherUserDoesNotExist_throwNotFoundException() {
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());

        assertThrows(NotFoundException.class, () -> this.userService.getCommonFriends(user.getId(), 999L));
        assertThrows(NotFoundException.class, () -> this.userService.getCommonFriends(999L, user.getId()));
    }

    @Test
    @DisplayName("addUser: name is empty -> apply login as name")
    void addUser_nameIsEmpty_applyLoginAsName() {
        User user = TestDataFactory.createValidUser();
        user.setName("");

        User added = this.userService.addUser(user);

        assertEquals(user.getLogin(), added.getName());
    }

    @Test
    @DisplayName("getCommonFriends: return common friends")
    void getCommonFriends_returnCommonFriends() {
        User user1 = this.userStorage.addUser(TestDataFactory.createValidUser());
        User user2 = this.userStorage.addUser(TestDataFactory.createValidUser());
        User commonFriend = this.userStorage.addUser(TestDataFactory.createValidUser());

        this.userService.addFriend(user1.getId(), commonFriend.getId());
        this.userService.addFriend(user2.getId(), commonFriend.getId());

        Collection<User> commonFriends = this.userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.iterator().next().getId());
    }
}
