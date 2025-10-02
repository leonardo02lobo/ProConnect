package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Proconnect.backProconnect.models.User;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.specification.UserSpecification;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String AddUser(User user) {
        try {
            if (user == null) {
                return "User cannot be null";
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return "User added successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public List<User> GetAllUsers() {
        return userRepository.findAll();
    }

    public String UpdateUserById(Long Id, User newUser) {
        try {
            if (Long.valueOf(Id) == null) {
                return "Id cannot be null";
            }
            if (!userRepository.existsById(Id)) {
                return "User with Id " + Id + " does not exist";
            }
            User existingUser = userRepository.findById(Id).orElse(null);
            if (existingUser == null) {
                return "User with Id " + Id + " does not exist";
            }
            existingUser.setName(newUser.getName());
            existingUser.setEmail(newUser.getEmail());
            existingUser.setPassword(newUser.getPassword());
            existingUser.setRole(newUser.getRole());
            existingUser.setUserName(newUser.getUserName());
            userRepository.save(existingUser);
            return "User with Id " + Id + " updated successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String DeleteUserById(Long Id) {
        try {
            if (Long.valueOf(Id) == null) {
                return "Id cannot be null";
            }
            if (!userRepository.existsById(Id)) {
                return "User with Id " + Id + " does not exist";
            }
            userRepository.deleteById(Id);
            return "User with Id " + Id + " deleted successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public User GetUserById(Long Id) {
        try {
            if (Long.valueOf(Id) == null) {
                return null;
            }
            Optional<User> user = userRepository.findById(Id);

            return user.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public List<User> SearchByNameOrUsername(String name) {
        return userRepository.findAll(UserSpecification.hasNameLike(name));
    }

    public User findUserByUserName(String username) {
        return userRepository.findByUserName(username);
    }
}
