package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {
    Long id;
    @NotBlank
    String name;
    @Length(max = 200)
    String description;
    @NotNull
    LocalDate releaseDate;
    @Positive
    Integer duration;
    Mpa mpa;
    Set<Genre> genres;
    Set<Director> directors;
}
