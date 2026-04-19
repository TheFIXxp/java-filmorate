package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.testutil.TestDataFactory;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmServiceTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private FeedService feedService;

    private Film createValidFilmWithMpa() {
        Film film = TestDataFactory.createValidFilm();
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);
        film.setGenres(new HashSet<>());
        return film;
    }

    @Test
    @DisplayName("getFilmById: film does not exist -> throw NotFoundException")
    void getFilmById_filmDoesNotExist_throwNotFoundException() {
        assertThrows(NotFoundException.class, () -> this.filmService.getFilmById(999L));
    }

    @Test
    @DisplayName("updateFilm: film does not exist -> throw NotFoundException")
    void updateFilm_filmDoesNotExist_throwNotFoundException() {
        Film film = createValidFilmWithMpa();
        film.setId(999L);
        assertThrows(NotFoundException.class, () -> this.filmService.updateFilm(film));
    }

    @Test
    @DisplayName("addLike: film does not exist -> throw NotFoundException")
    void addLike_filmDoesNotExist_throwNotFoundException() {
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());
        assertThrows(NotFoundException.class, () -> this.filmService.addLike(999L, user.getId()));
    }

    @Test
    @DisplayName("addLike: user does not exist -> throw NotFoundException")
    void addLike_userDoesNotExist_throwNotFoundException() {
        Film film = this.filmStorage.addFilm(createValidFilmWithMpa());
        assertThrows(NotFoundException.class, () -> this.filmService.addLike(film.getId(), 999L));
    }

    @Test
    @DisplayName("removeLike: film does not exist -> throw NotFoundException")
    void removeLike_filmDoesNotExist_throwNotFoundException() {
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());
        assertThrows(NotFoundException.class, () -> this.filmService.removeLike(999L, user.getId()));
    }

    @Test
    @DisplayName("removeLike: user does not exist -> throw NotFoundException")
    void removeLike_userDoesNotExist_throwNotFoundException() {
        Film film = this.filmStorage.addFilm(createValidFilmWithMpa());
        assertThrows(NotFoundException.class, () -> this.filmService.removeLike(film.getId(), 999L));
    }

    @Test
    @DisplayName("addFilm: release date is too early -> throw ValidationException")
    void addFilm_releaseDateIsTooEarly_throwValidationException() {
        Film film = createValidFilmWithMpa();
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        assertThrows(ValidationException.class, () -> this.filmService.addFilm(film));
    }

    @Test
    @DisplayName("getPopular: return films sorted by likes count")
    void getPopular_returnFilmsSortedByLikesCount() {
        Film film1 = this.filmStorage.addFilm(createValidFilmWithMpa());
        Film film2 = this.filmStorage.addFilm(createValidFilmWithMpa());
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());

        this.filmService.addLike(film2.getId(), user.getId());

        List<Film> popular = (List<Film>) this.filmService.getPopular(10);

        assertEquals(2, popular.size());
        assertEquals(film2.getId(), popular.get(0).getId());
        assertEquals(film1.getId(), popular.get(1).getId());
    }

    @Test
    @DisplayName("getCommon: return common films of two users sorted by likes count")
    void getCommon_returnCommonFilmsSortedByLikesCount() {
        Film film1 = this.filmStorage.addFilm(createValidFilmWithMpa());
        Film film2 = this.filmStorage.addFilm(createValidFilmWithMpa());
        Film film3 = this.filmStorage.addFilm(createValidFilmWithMpa());

        User user1 = this.userStorage.addUser(TestDataFactory.createValidUser());
        User user2 = this.userStorage.addUser(TestDataFactory.createValidUser2());
        User user3 = this.userStorage.addUser(TestDataFactory.createValidUser3());

        this.filmService.addLike(film1.getId(), user1.getId());
        this.filmService.addLike(film2.getId(), user1.getId());

        this.filmService.addLike(film1.getId(), user2.getId());
        this.filmService.addLike(film2.getId(), user2.getId());
        this.filmService.addLike(film3.getId(), user2.getId());

        this.filmService.addLike(film2.getId(), user3.getId());

        List<Film> common = (List<Film>) this.filmService.getCommon(user1.getId(), user2.getId());

        assertEquals(2, common.size());


        assertEquals(film2.getId(), common.get(0).getId());
        assertEquals(film1.getId(), common.get(1).getId());
    }

    @Test
    @DisplayName("getCommon: user does not exist -> throw NotFoundException")
    void getCommon_userDoesNotExist_throwNotFoundException() {
        assertThrows(NotFoundException.class, () -> this.filmService.getCommon(999L, 1L));
    }

    @Test
    @DisplayName("getCommon: friend does not exist -> throw NotFoundException")
    void getCommon_friendDoesNotExist_throwNotFoundException() {
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());
        assertThrows(NotFoundException.class, () -> this.filmService.getCommon(user.getId(), 999L));
    }

    @Test
    @DisplayName("addLike: should create LIKE/ADD event")
    void addLike_shouldCreateLikeAddEvent() {
        Film film = this.filmStorage.addFilm(createValidFilmWithMpa());
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());

        this.filmService.addLike(film.getId(), user.getId());

        Collection<Event> feed = this.feedService.getFeedByUserId(user.getId());
        assertEquals(1, feed.size());

        Event event = feed.iterator().next();
        assertEquals(user.getId(), event.getUserId());
        assertEquals(Event.EventType.LIKE, event.getEventType());
        assertEquals(Event.Operation.ADD, event.getOperation());
        assertEquals(film.getId(), event.getEntityId());
    }

    @Test
    @DisplayName("removeLike: should create LIKE/REMOVE event")
    void removeLike_shouldCreateLikeRemoveEvent() {
        Film film = this.filmStorage.addFilm(createValidFilmWithMpa());
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());
        this.filmService.addLike(film.getId(), user.getId());

        this.filmService.removeLike(film.getId(), user.getId());

        Collection<Event> feed = this.feedService.getFeedByUserId(user.getId());
        var events = feed.stream().toList();

        assertEquals(2, events.size());
        Event removeEvent = events.getLast();
        assertEquals(Event.Operation.REMOVE, removeEvent.getOperation());
        assertEquals(film.getId(), removeEvent.getEntityId());
    }

    @Test
    @DisplayName("addLike and removeLike: should create separate events for each operation")
    void addLikeAndRemoveLike_shouldCreateSeparateEventsForEachOperation() {
        Film film = this.filmStorage.addFilm(createValidFilmWithMpa());
        User user = this.userStorage.addUser(TestDataFactory.createValidUser());

        this.filmService.addLike(film.getId(), user.getId());
        this.filmService.removeLike(film.getId(), user.getId());
        this.filmService.addLike(film.getId(), user.getId());

        Collection<Event> feed = this.feedService.getFeedByUserId(user.getId());
        assertEquals(3, feed.size());

        var events = feed.stream().toList();
        assertEquals(Event.Operation.ADD, events.get(0).getOperation());
        assertEquals(Event.Operation.REMOVE, events.get(1).getOperation());
        assertEquals(Event.Operation.ADD, events.get(2).getOperation());
    }
}
