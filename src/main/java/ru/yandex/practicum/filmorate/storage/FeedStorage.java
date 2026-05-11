package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

public interface FeedStorage {

    void addEvent(Event event);

    Collection<Event> getFeedByUserId(long userId);
}
