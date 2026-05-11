package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedService {

    final FeedStorage feedStorage;

    public void addEvent(long userId, Event.EventType eventType, Event.Operation operation, long entityId) {
        Event event = new Event();
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setEntityId(entityId);

        this.feedStorage.addEvent(event);
        log.info("Event added: userId={}, eventType={}, operation={}, entityId={}", userId, eventType, operation,
                 entityId);
    }

    public Collection<Event> getFeedByUserId(long userId) {
        log.info("Get feed for userId={}", userId);
        return this.feedStorage.getFeedByUserId(userId);
    }
}
