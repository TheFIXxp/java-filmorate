package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewService {

    final ReviewStorage reviewStorage;
    final UserService userService;
    final FilmService filmService;
    final FeedService feedService;

    @Transactional
    public Review create(Review review) {
        userService.ensureUserExists(review.getUserId());
        filmService.ensureFilmExists(review.getFilmId());

        review.setUseful(0);
        Review created = reviewStorage.create(review);
        this.feedService.addEvent(created.getUserId(), Event.EventType.REVIEW, Event.Operation.ADD, created.getReviewId());
        log.info("Created review id={} for filmId={} by userId={}",
                created.getReviewId(), created.getFilmId(), created.getUserId());
        return created;
    }

    @Transactional(readOnly = true)
    public List<Review> readAll(Long filmId, Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;
        log.debug("Reading reviews: filmId={}, count={}, limit={}", filmId, count, limit);
        List<Review> reviews = reviewStorage.findAllByFilmId(filmId, limit);
        log.debug("Returned {} reviews", reviews.size());
        return reviews;
    }

    @Transactional(readOnly = true)
    public Review readOne(long id) {
        log.debug("Fetching review id={}", id);
        return reviewStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Review with id={} not found", id);
                    return new NotFoundException("Review with id " + id + " not found");
                });
    }

    @Transactional
    public Review update(Review review) {
        ensureReviewExists(review.getReviewId());

        Review existing = readOne(review.getReviewId());
        review.setUserId(existing.getUserId());
        review.setFilmId(existing.getFilmId());
        review.setUseful(existing.getUseful());

        userService.ensureUserExists(review.getUserId());
        filmService.ensureFilmExists(review.getFilmId());

        Review updated = reviewStorage.update(review);
        this.feedService.addEvent(updated.getUserId(), Event.EventType.REVIEW, Event.Operation.UPDATE, updated.getReviewId());
        log.info("Updated review id={}", updated.getReviewId());
        return updated;
    }

    @Transactional
    public void delete(long reviewId) {
        ensureReviewExists(reviewId);
        Review review = readOne(reviewId);
        reviewStorage.delete(reviewId);
        this.feedService.addEvent(review.getUserId(), Event.EventType.REVIEW, Event.Operation.REMOVE, reviewId);
        log.info("Deleted review id={}", reviewId);
    }

    @Transactional
    public void addLike(long reviewId, long userId) {
        ensureReviewExists(reviewId);
        userService.ensureUserExists(userId);
        reviewStorage.addLike(reviewId, userId);
        log.info("User {} liked review {}", userId, reviewId);
    }

    @Transactional
    public void removeLike(long reviewId, long userId) {
        ensureReviewExists(reviewId);
        userService.ensureUserExists(userId);
        reviewStorage.removeLike(reviewId, userId);
        log.info("User {} removed like from review {}", userId, reviewId);
    }

    @Transactional
    public void addDislike(long reviewId, long userId) {
        ensureReviewExists(reviewId);
        userService.ensureUserExists(userId);
        reviewStorage.addDislike(reviewId, userId);
        log.info("User {} disliked review {}", userId, reviewId);
    }

    @Transactional
    public void removeDislike(long reviewId, long userId) {
        ensureReviewExists(reviewId);
        userService.ensureUserExists(userId);
        reviewStorage.removeDislike(reviewId, userId);
        log.info("User {} removed dislike from review {}", userId, reviewId);
    }

    @Transactional
    public void ensureReviewExists(long reviewId) {
        log.debug("Checking existence of review id={}", reviewId);
        reviewStorage.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("Review with id={} not found", reviewId);
                    return new NotFoundException("Review with id %s not found".formatted(reviewId));
                });
        log.debug("Review id={} exists", reviewId);
    }
}