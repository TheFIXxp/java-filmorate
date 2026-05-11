package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FeedServiceTest {

    @Autowired
    private FeedService feedService;

    @Autowired
    private UserDbStorage userStorage;

    @Test
    @DisplayName("addEvent: should add event with correct fields")
    void addEvent_shouldAddEventWithCorrectFields() {
        long userId = this.userStorage.addUser(TestDataFactory.createValidUser()).getId();
        long entityId = 10L;
        long beforeTimestamp = System.currentTimeMillis();

        this.feedService.addEvent(userId, Event.EventType.FRIEND, Event.Operation.ADD, entityId);

        Collection<Event> feed = this.feedService.getFeedByUserId(userId);
        assertEquals(1, feed.size());

        Event event = feed.iterator().next();
        assertEquals(userId, event.getUserId());
        assertEquals(Event.EventType.FRIEND, event.getEventType());
        assertEquals(Event.Operation.ADD, event.getOperation());
        assertEquals(entityId, event.getEntityId());
        assertTrue(event.getTimestamp() >= beforeTimestamp);
        assertTrue(event.getEventId() > 0);
    }

    @Test
    @DisplayName("addEvent: should generate unique eventIds")
    void addEvent_shouldGenerateUniqueEventIds() {
        long userId = this.userStorage.addUser(TestDataFactory.createValidUser()).getId();

        this.feedService.addEvent(userId, Event.EventType.FRIEND, Event.Operation.ADD, 1L);
        this.feedService.addEvent(userId, Event.EventType.LIKE, Event.Operation.ADD, 2L);
        this.feedService.addEvent(userId, Event.EventType.FRIEND, Event.Operation.REMOVE, 3L);

        Collection<Event> feed = this.feedService.getFeedByUserId(userId);
        assertEquals(3, feed.size());

        var eventIds = feed.stream().map(Event::getEventId).distinct().count();
        assertEquals(3, eventIds);
    }

    @Test
    @DisplayName("getFeedByUserId: should return empty collection when no events exist")
    void getFeedByUserId_shouldReturnEmptyCollectionWhenNoEventsExist() {
        User user = TestDataFactory.createValidUser();
        long userId = this.userStorage.addUser(user).getId();

        Collection<Event> feed = this.feedService.getFeedByUserId(userId);

        assertEquals(0, feed.size());
    }

    @Test
    @DisplayName("getFeedByUserId: should return events sorted by timestamp ASC")
    void getFeedByUserId_shouldReturnEventsSortedByTimestampDesc() throws InterruptedException {
        User user = TestDataFactory.createValidUser();
        long userId = this.userStorage.addUser(user).getId();

        this.feedService.addEvent(userId, Event.EventType.FRIEND, Event.Operation.ADD, 1L);
        this.feedService.addEvent(userId, Event.EventType.LIKE, Event.Operation.ADD, 2L);
        this.feedService.addEvent(userId, Event.EventType.FRIEND, Event.Operation.REMOVE, 3L);

        Collection<Event> feed = this.feedService.getFeedByUserId(userId);
        var events = feed.stream().toList();

        assertEquals(3, events.size());
        assertTrue(events.get(0).getTimestamp() <= events.get(1).getTimestamp());
        assertTrue(events.get(1).getTimestamp() <= events.get(2).getTimestamp());
    }

    @Test
    @DisplayName("getFeedByUserId: should return only events for specific user")
    void getFeedByUserId_shouldReturnOnlyEventsForSpecificUser() {
        long userId1 = this.userStorage.addUser(TestDataFactory.createValidUser()).getId();
        long userId2 = this.userStorage.addUser(TestDataFactory.createValidUser2()).getId();

        this.feedService.addEvent(userId1, Event.EventType.FRIEND, Event.Operation.ADD, 1L);
        this.feedService.addEvent(userId2, Event.EventType.FRIEND, Event.Operation.ADD, 2L);
        this.feedService.addEvent(userId1, Event.EventType.LIKE, Event.Operation.ADD, 3L);

        Collection<Event> feed1 = this.feedService.getFeedByUserId(userId1);
        Collection<Event> feed2 = this.feedService.getFeedByUserId(userId2);

        assertEquals(2, feed1.size());
        assertEquals(1, feed2.size());

        feed1.forEach(event -> assertEquals(userId1, event.getUserId()));
        feed2.forEach(event -> assertEquals(userId2, event.getUserId()));
    }

    @Test
    @DisplayName("addEvent: should support all EventType values")
    void addEvent_shouldSupportAllEventTypeValues() {
        long userId = this.userStorage.addUser(TestDataFactory.createValidUser()).getId();

        for (Event.EventType eventType : Event.EventType.values()) {
            this.feedService.addEvent(userId, eventType, Event.Operation.ADD, 1L);
        }

        Collection<Event> feed = this.feedService.getFeedByUserId(userId);
        assertEquals(Event.EventType.values().length, feed.size());
    }

    @Test
    @DisplayName("addEvent: should support all Operation values")
    void addEvent_shouldSupportAllOperationValues() {
        long userId = this.userStorage.addUser(TestDataFactory.createValidUser()).getId();

        for (Event.Operation operation : Event.Operation.values()) {
            this.feedService.addEvent(userId, Event.EventType.LIKE, operation, 1L);
        }

        Collection<Event> feed = this.feedService.getFeedByUserId(userId);
        assertEquals(Event.Operation.values().length, feed.size());
    }
}

