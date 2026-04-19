package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.mappers.EventRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private static final String INSERT_EVENT = "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_FEED_BY_USER_ID = "SELECT event_id, timestamp, user_id, event_type, operation, entity_id FROM events WHERE user_id = ? ORDER BY timestamp ASC, event_id ASC";

    private final JdbcTemplate jdbcTemplate;
    private final EventRowMapper eventRowMapper;

    @Override
    public void addEvent(Event event) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_EVENT, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, event.getTimestamp());
            ps.setLong(2, event.getUserId());
            ps.setString(3, event.getEventType().toString());
            ps.setString(4, event.getOperation().toString());
            ps.setLong(5, event.getEntityId());
            return ps;
        }, keyHolder);

        event.setEventId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.debug("Event added: eventId={}, userId={}, eventType={}, operation={}",
                  event.getEventId(), event.getUserId(), event.getEventType(), event.getOperation());
    }

    @Override
    public Collection<Event> getFeedByUserId(long userId) {
        log.info("Get feed for userId={}", userId);
        return this.jdbcTemplate.query(SELECT_FEED_BY_USER_ID, this.eventRowMapper, userId);
    }
}

