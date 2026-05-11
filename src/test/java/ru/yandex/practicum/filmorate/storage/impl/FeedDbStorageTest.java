package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FeedDbStorageTest {

    @Autowired
    private FeedDbStorage feedDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    @DisplayName("addEvent: should persist event to database")
    void addEvent_shouldPersistEventToDatabase() {
        long userId = this.userDbStorage.addUser(TestDataFactory.createValidUser()).getId();
        Event event = TestDataFactory.createEvent(userId, Event.EventType.FRIEND, Event.Operation.ADD, 100L);

        this.feedDbStorage.addEvent(event);

        assertNotEquals(0, event.getEventId());
        assertTrue(event.getEventId() > 0);
    }

    @Test
    @DisplayName("addEvent: should set generated eventId")
    void addEvent_shouldSetGeneratedEventId() {
        long userId = this.userDbStorage.addUser(TestDataFactory.createValidUser()).getId();
        Event event1 = TestDataFactory.createEvent(userId, Event.EventType.LIKE, Event.Operation.ADD, 1L);
        Event event2 = TestDataFactory.createEvent(userId, Event.EventType.FRIEND, Event.Operation.ADD, 2L);

        this.feedDbStorage.addEvent(event1);
        this.feedDbStorage.addEvent(event2);

        assertNotEquals(event1.getEventId(), event2.getEventId());
        assertTrue(event1.getEventId() < event2.getEventId());
    }

    @Test
    @DisplayName("getFeedByUserId: should return empty collection when no events")
    void getFeedByUserId_shouldReturnEmptyCollectionWhenNoEvents() {
        long userId = this.userDbStorage.addUser(TestDataFactory.createValidUser()).getId();

        Collection<Event> feed = this.feedDbStorage.getFeedByUserId(userId);

        assertEquals(0, feed.size());
    }

    @Test
    @DisplayName("getFeedByUserId: should retrieve added events")
    void getFeedByUserId_shouldRetrieveAddedEvents() {
        long userId = this.userDbStorage.addUser(TestDataFactory.createValidUser()).getId();
        Event event = TestDataFactory.createEvent(userId, Event.EventType.FRIEND, Event.Operation.ADD, 100L);

        this.feedDbStorage.addEvent(event);
        Collection<Event> feed = this.feedDbStorage.getFeedByUserId(userId);

        assertEquals(1, feed.size());
        Event retrievedEvent = feed.iterator().next();
        assertEquals(event.getEventId(), retrievedEvent.getEventId());
        assertEquals(userId, retrievedEvent.getUserId());
        assertEquals(Event.EventType.FRIEND, retrievedEvent.getEventType());
        assertEquals(Event.Operation.ADD, retrievedEvent.getOperation());
        assertEquals(100L, retrievedEvent.getEntityId());
    }

    @Test
    @DisplayName("getFeedByUserId: should sort by timestamp ASC")
    void getFeedByUserId_shouldSortByTimestampDesc() {
        long userId = this.userDbStorage.addUser(TestDataFactory.createValidUser()).getId();
        Event event1 = TestDataFactory.createEvent(userId, 1000L, Event.EventType.FRIEND, Event.Operation.ADD, 1L);
        Event event2 = TestDataFactory.createEvent(userId, 3000L, Event.EventType.LIKE, Event.Operation.ADD, 2L);
        Event event3 = TestDataFactory.createEvent(userId, 2000L, Event.EventType.FRIEND, Event.Operation.REMOVE, 3L);

        this.feedDbStorage.addEvent(event1);
        this.feedDbStorage.addEvent(event2);
        this.feedDbStorage.addEvent(event3);

        Collection<Event> feed = this.feedDbStorage.getFeedByUserId(userId);
        var events = feed.stream().toList();

        assertEquals(3, events.size());
        assertEquals(1000L, events.get(0).getTimestamp());
        assertEquals(2000L, events.get(1).getTimestamp());
        assertEquals(3000L, events.get(2).getTimestamp());
    }

    @Test
    @DisplayName("getFeedByUserId: should return only events for specific user")
    void getFeedByUserId_shouldReturnOnlyEventsForSpecificUser() {
        long userId1 = this.userDbStorage.addUser(TestDataFactory.createValidUser()).getId();
        long userId2 = this.userDbStorage.addUser(TestDataFactory.createValidUser2()).getId();

        Event event1 = TestDataFactory.createEvent(userId1, Event.EventType.FRIEND, Event.Operation.ADD, 1L);
        Event event2 = TestDataFactory.createEvent(userId2, Event.EventType.FRIEND, Event.Operation.ADD, 2L);

        this.feedDbStorage.addEvent(event1);
        this.feedDbStorage.addEvent(event2);

        Collection<Event> feed1 = this.feedDbStorage.getFeedByUserId(userId1);
        Collection<Event> feed2 = this.feedDbStorage.getFeedByUserId(userId2);

        assertEquals(1, feed1.size());
        assertEquals(1, feed2.size());
        assertEquals(userId1, feed1.iterator().next().getUserId());
        assertEquals(userId2, feed2.iterator().next().getUserId());
    }

    @Test
    @DisplayName("getFeedByUserId: should handle multiple events correctly")
    void getFeedByUserId_shouldHandleMultipleEventsCorrectly() {
        long userId = this.userDbStorage.addUser(TestDataFactory.createValidUser()).getId();

        for (int i = 0; i < 10; i++) {
            Event event = TestDataFactory.createEvent(userId, Event.EventType.LIKE, Event.Operation.ADD, i);
            this.feedDbStorage.addEvent(event);
        }

        Collection<Event> feed = this.feedDbStorage.getFeedByUserId(userId);

        assertEquals(10, feed.size());
    }
}



