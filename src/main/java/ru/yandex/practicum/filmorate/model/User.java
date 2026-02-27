package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    long id;
    @Email
    @NotBlank
    String email;
    @NotBlank
    @Pattern(regexp = "^\\S+", message = "Login must not contain spaces")
    String login;
    String name;
    @NotNull
    @Past
    LocalDate birthday;
}
