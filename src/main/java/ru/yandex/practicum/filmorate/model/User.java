package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Data
public class User {
    int id;
    @Email
    String email;
    @NotBlank
    @Pattern(regexp = "^\\S+", message = "Login must not contain spaces")
    String login;
    String name;
    @NotNull
    @Past
    LocalDate birthday;
}
