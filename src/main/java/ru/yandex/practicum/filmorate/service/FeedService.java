package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedService {

    final FeedStorage feedStorage;
    final UserStorage userStorage;

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
        this.userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));
        return this.feedStorage.getFeedByUserId(userId);
    }
}
