package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper reviewRowMapper;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        review.setReviewId(id);
        log.debug("Created review with id {}", id);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        log.debug("Updated review id {}", review.getReviewId());

        return review;
    }

    @Override
    public void delete(long reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, reviewId);
        log.debug("Deleted review id {}", reviewId);
    }

    @Override
    public Optional<Review> findById(long reviewId) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            Review review = jdbcTemplate.queryForObject(sql, reviewRowMapper, reviewId);
            log.debug("Found review by id={}", reviewId);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Review with id={} not found in DB", reviewId);
            return Optional.empty();
        }
    }

    @Override
    public List<Review> findAllByFilmId(Long filmId, int count) {
        String sql;
        Object[] params;
        if (filmId == null) {
            sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
            params = new Object[]{count};
            log.debug("Executing query to fetch top {} reviews", count);
        } else {
            sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
            params = new Object[]{filmId, count};
            log.debug("Executing query for filmId={}, limit={}", filmId, count);
        }
        List<Review> reviews = jdbcTemplate.query(sql, reviewRowMapper, params);
        log.debug("Fetched {} reviews", reviews.size());
        return reviews;
    }

    @Override
    public void addLike(long reviewId, long userId) {
        String currentRatingSql = "SELECT rating_type FROM review_ratings WHERE review_id = ? AND user_id = ?";
        String currentRating = null;
        try {
            currentRating = jdbcTemplate.queryForObject(currentRatingSql, String.class, reviewId, userId);
        } catch (EmptyResultDataAccessException ignored) {
        }

        if ("LIKE".equals(currentRating)) {
            log.debug("User {} already liked review {}, skipping", userId, reviewId);
            return;
        } else if ("DISLIKE".equals(currentRating)) {
            log.debug("User {} had dislike on review {}, converting to like", userId, reviewId);
            removeDislike(reviewId, userId);
        }

        String insertSql = "INSERT INTO review_ratings (review_id, user_id, rating_type) VALUES (?, ?, 'LIKE')";
        jdbcTemplate.update(insertSql, reviewId, userId);

        int delta = 1;
        String updateUsefulSql = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        jdbcTemplate.update(updateUsefulSql, delta, reviewId);

        log.debug("User {} liked review {}, delta={}", userId, reviewId, delta);
    }

    @Override
    public void addDislike(long reviewId, long userId) {
        String currentRatingSql = "SELECT rating_type FROM review_ratings WHERE review_id = ? AND user_id = ?";
        String currentRating = null;
        try {
            currentRating = jdbcTemplate.queryForObject(currentRatingSql, String.class, reviewId, userId);
        } catch (EmptyResultDataAccessException ignored) {
        }

        if ("DISLIKE".equals(currentRating)) {
            log.debug("User {} already disliked review {}, skipping", userId, reviewId);
            return;
        }
        if ("LIKE".equals(currentRating)) {
            log.debug("User {} had like on review {}, converting to dislike", userId, reviewId);
            removeLike(reviewId, userId);
        }

        String insertSql = "INSERT INTO review_ratings (review_id, user_id, rating_type) VALUES (?, ?, 'DISLIKE')";
        jdbcTemplate.update(insertSql, reviewId, userId);

        int delta = -1;
        String updateUsefulSql = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        jdbcTemplate.update(updateUsefulSql, delta, reviewId);

        log.debug("User {} disliked review {}, delta={}", userId, reviewId, delta);
    }

    @Override
    public void removeLike(long reviewId, long userId) {
        String deleteSql = "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ? AND rating_type = 'LIKE'";
        int rows = jdbcTemplate.update(deleteSql, reviewId, userId);
        if (rows > 0) {
            String updateSql = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
            jdbcTemplate.update(updateSql, reviewId);
            log.debug("User {} removed like from review {}", userId, reviewId);
        } else {
            log.debug("User {} had no like on review {}, nothing removed", userId, reviewId);
        }
    }

    @Override
    public void removeDislike(long reviewId, long userId) {
        String deleteSql = "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ? AND rating_type = 'DISLIKE'";
        int rows = jdbcTemplate.update(deleteSql, reviewId, userId);
        if (rows > 0) {
            String updateSql = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
            jdbcTemplate.update(updateSql, reviewId);
            log.debug("User {} removed dislike from review {}", userId, reviewId);
        } else {
            log.debug("User {} had no dislike on review {}, nothing removed", userId, reviewId);
        }
    }
}