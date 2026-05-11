package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private FeedService feedService;

    @Test
    @DisplayName("addFriend: should create FRIEND/ADD event")
    void addFriend_shouldCreateFriendAddEvent() {
        User user1 = this.userStorage.addUser(TestDataFactory.createValidUser());
        User user2 = this.userStorage.addUser(TestDataFactory.createValidUser2());

        this.userService.addFriend(user1.getId(), user2.getId());

        Collection<Event> feed = this.feedService.getFeedByUserId(user1.getId());
        assertEquals(1, feed.size());

        Event event = feed.iterator().next();
        assertEquals(user1.getId(), event.getUserId());
        assertEquals(Event.EventType.FRIEND, event.getEventType());
        assertEquals(Event.Operation.ADD, event.getOperation());
        assertEquals(user2.getId(), event.getEntityId());
    }

    @Test
    @DisplayName("removeFriend: should create FRIEND/REMOVE event")
    void removeFriend_shouldCreateFriendRemoveEvent() {
        User user1 = this.userStorage.addUser(TestDataFactory.createValidUser());
        User user2 = this.userStorage.addUser(TestDataFactory.createValidUser2());
        this.userService.addFriend(user1.getId(), user2.getId());
        this.userService.removeFriend(user1.getId(), user2.getId());

        Collection<Event> feed = this.feedService.getFeedByUserId(user1.getId());
        var events = feed.stream().toList();

        assertEquals(2, events.size());
        Event removeEvent = events.getLast();
        assertEquals(Event.Operation.REMOVE, removeEvent.getOperation());
        assertEquals(user2.getId(), removeEvent.getEntityId());
    }

    @Test
    @DisplayName("addFriend and removeFriend: should create separate events for each operation")
    void addFriendAndRemoveFriend_shouldCreateSeparateEventsForEachOperation() {
        User user1 = this.userStorage.addUser(TestDataFactory.createValidUser());
        User user2 = this.userStorage.addUser(TestDataFactory.createValidUser2());

        this.userService.addFriend(user1.getId(), user2.getId());
        this.userService.addFriend(user1.getId(), user2.getId());
        this.userService.removeFriend(user1.getId(), user2.getId());

        Collection<Event> feed = this.feedService.getFeedByUserId(user1.getId());
        assertEquals(3, feed.size());

        var events = feed.stream().toList();
        assertEquals(Event.Operation.ADD, events.get(0).getOperation());
        assertEquals(Event.Operation.ADD, events.get(1).getOperation());
        assertEquals(Event.Operation.REMOVE, events.get(2).getOperation());
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
        User user2 = this.userStorage.addUser(TestDataFactory.createValidUser2());
        User commonFriend = this.userStorage.addUser(TestDataFactory.createValidUser3());

        this.userService.addFriend(user1.getId(), commonFriend.getId());
        this.userService.addFriend(user2.getId(), commonFriend.getId());

        Collection<User> commonFriends = this.userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.iterator().next().getId());
    }
}
