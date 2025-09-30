package Proconnect.backProconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.models.User;
import Proconnect.backProconnect.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> AddUser(@RequestBody @Valid User user){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.AddUser(user));
    }
    @GetMapping
    public ResponseEntity<?> GetAllUser(){
        return ResponseEntity.ok().body(userService.GetAllUsers());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> UpdateUserById(@PathVariable Long id,@RequestBody @Valid User newUser){
        return ResponseEntity.status(HttpStatus.OK).body(userService.UpdateUserById(id, newUser));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> DeleteUserById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(userService.DeleteUserById(id));
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<?> GetUserById(@PathVariable Long id){
        return ResponseEntity.ok().body(userService.GetUserById(id));
    }

    @GetMapping("/search/name/{name}")
    public ResponseEntity<?> SearchByNameOrUsername(@PathVariable String name){
        return ResponseEntity.ok().body(userService.SearchByNameOrUsername(name));
    }
}
