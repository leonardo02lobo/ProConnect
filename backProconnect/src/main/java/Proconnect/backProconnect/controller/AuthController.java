package Proconnect.backProconnect.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.AuthDto;
import Proconnect.backProconnect.model.JwtResponse;
import Proconnect.backProconnect.model.UserDto;
import Proconnect.backProconnect.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto user) {
        boolean response = userService.AddUser(user);

        if (!response) {
            return ResponseEntity.badRequest().body("Error en el body de la request");
        }
        return ResponseEntity.ok().body("Usuario Agregado");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto auth) {
        try {
            boolean authValid = userService.authUser(auth.getEmail(), auth.getPassword());

            if (!authValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas al momento de loguearte");
            }
            Optional<User> user = userService.getUserByEmail(auth.getEmail());

            if (user.isPresent()) {
                User usuario = user.get();
                return ResponseEntity.ok(new JwtResponse(null, new UserDto(usuario.getId(), usuario.getName(), usuario.getUserName(), usuario.getEmail(), usuario.getPassword(), usuario.getRole(),null)));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
