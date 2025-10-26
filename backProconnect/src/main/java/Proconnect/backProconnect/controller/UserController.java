package Proconnect.backProconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.model.UserDto;
import Proconnect.backProconnect.model.UserProfileDTO;
import Proconnect.backProconnect.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> AddUser(@RequestBody @Valid UserDto user) {
        Object createdUser = userService.AddUser(user);
        if (createdUser != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User could not be created.");
        }
    }

    @GetMapping
    public ResponseEntity<?> GetAllUser() {
        Object users = userService.GetAllUsers();
        if (users != null) {
            return ResponseEntity.ok().body(users);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users found.");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> UpdateUserById(@PathVariable Long id, @RequestBody @Valid UserDto newUser) {
        Object updatedUser = userService.UpdateUserById(id, newUser);
        if (updatedUser != null) {
            return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> DeleteUserById(@PathVariable Long id) {
        boolean deleted = userService.DeleteUserById(id);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<?> GetUserById(@PathVariable Long id) {
        Object user = userService.GetUserById(id);
        if (user != null) {
            return ResponseEntity.ok().body(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @GetMapping("/search/name/{name}")
    public ResponseEntity<?> SearchByNameOrUsername(@PathVariable String name) {
        Object users = userService.SearchByNameOrUsername(name);
        if (users != null) {
            return ResponseEntity.ok().body(users);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users found.");
        }
    }

    @GetMapping("/{userId}/profiles-posts")
    public UserProfileDTO getUserProfilesAndPosts(@PathVariable Long userId) {
        return userService.findUsersWithProfilesAndPosts(userId);
    }
}
