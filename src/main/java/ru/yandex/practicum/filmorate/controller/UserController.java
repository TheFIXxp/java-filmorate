package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/users")
public class UserController {

    UserService userService;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return this.userService.addUser(user);
    }

    @GetMapping
    public Collection<User> getUsers() {
        return this.userService.getUsers();
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return this.userService.updateUser(user);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return this.userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        this.userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable long id, @PathVariable long friendId) {
        this.userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable long id) {
        return this.userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return this.userService.getCommonFriends(id, otherId);
    }
}
