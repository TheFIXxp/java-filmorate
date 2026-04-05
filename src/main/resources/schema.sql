CREATE TABLE IF NOT EXISTS mpa
(
    id   INT PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users
(
    id       IDENTITY PRIMARY KEY,
    email    VARCHAR(100) NOT NULL UNIQUE,
    login    VARCHAR(100) NOT NULL UNIQUE,
    name     VARCHAR(100),
    birthday DATE         NOT NULL
);

CREATE TABLE IF NOT EXISTS films
(
    id           IDENTITY PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(200),
    release_date DATE         NOT NULL,
    duration     INT          NOT NULL,
    mpa_id       INT,
    FOREIGN KEY (mpa_id) REFERENCES mpa (id)
);

CREATE TABLE IF NOT EXISTS genres
(
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS film_genres
(
    film_id  BIGINT NOT NULL,
    genre_id INT    NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres (id)
);

CREATE TABLE IF NOT EXISTS friendships
(
    user_id   BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS film_likes
(
    film_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


