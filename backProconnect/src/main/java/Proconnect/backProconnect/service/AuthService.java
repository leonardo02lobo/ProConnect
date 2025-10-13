package Proconnect.backProconnect.service;


import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.LoginResponseDto;
import Proconnect.backProconnect.model.UserDto;
import Proconnect.backProconnect.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDto login(String email, String password){
        if(email == null || email.isEmpty()){
            return new LoginResponseDto(false, "email Or UserName null", null);
        }
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if(!userByEmail.isPresent()){
            return new LoginResponseDto(false,"Email not found", null);
        }


        User user = userByEmail.get();
        if(passwordEncoder.matches(password, user.getPassword())){
            return new LoginResponseDto(true, "login successufly", new UserDto(user.getId(), user.getName(), user.getUserName(), user.getEmail(), null, user.getRole()));
        }else{
            return new LoginResponseDto(false, "Incorrect Password", null);
        }

    }
}
