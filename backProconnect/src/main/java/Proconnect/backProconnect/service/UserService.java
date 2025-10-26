package Proconnect.backProconnect.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.ProfilePhotos;
import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.CommentsDto;
import Proconnect.backProconnect.model.PostsDto;
import Proconnect.backProconnect.model.ProfilePhotosDto;
import Proconnect.backProconnect.model.UserDto;
import Proconnect.backProconnect.model.UserProfileDTO;
import Proconnect.backProconnect.repository.ProfilePhotosRepository;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.specification.UserSpecification;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfilePhotosRepository profilePhotosRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ProfilePhotosRepository profilePhotosRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profilePhotosRepository = profilePhotosRepository;
    }

    public boolean AddUser(UserDto user) {
        try {
            if (user == null) {
                return false;
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User userEntity = new User(user.getId(), user.getName(), user.getUserName(), user.getEmail(),
                    user.getPassword(), user.getRole());
            userRepository.save(userEntity);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<User> GetAllUsers() {
        return userRepository.findAll();
    }

    public String UpdateUserById(Long Id, UserDto newUser) {
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
            existingUser.setId(newUser.getId());
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

    public boolean DeleteUserById(Long Id) {
        try {
            if (Long.valueOf(Id) == null) {
                return false;
            }
            if (!userRepository.existsById(Id)) {
                return false;
            }
            userRepository.deleteById(Id);
            return true;
        } catch (Exception e) {
            return false;
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

    public UserDto findUserByUserName(String username) {
        Optional<User> userOptional = userRepository.findByUserName(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return new UserDto(user.getId(), user.getName(), user.getUserName(), user.getEmail(), user.getPassword(),
                    user.getRole(),null);
        }
        return null;
    }

    public boolean authUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            return false;
        }
        User user = userOptional.get();
        return passwordEncoder.matches(password, user.getPassword());
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserProfileDTO findUsersWithProfilesAndPosts(Long userId) {
        List<Object[]> results = userRepository.findUsersWithProfilesAndPosts(userId);
        if (results.isEmpty())
            return null;

        Object[] first = results.get(0);

        Map<Long, PostsDto> postsMap = new HashMap<>();
        Map<Long, List<CommentsDto>> commentsMap = new HashMap<>();

        for (Object[] row : results) {
            Long postId = ((Number) row[7]).longValue();

            if (postId != null && !postsMap.containsKey(postId)) {
                PostsDto post = new PostsDto(
                        postId,
                        ((Number) row[0]).longValue(), 
                        (String) row[9], 
                        (String) row[8], 
                        (String) row[11], 
                        (java.util.Date) row[10], 
                        ((Number) row[16]).longValue(),
                        ((Number) row[17]).longValue(), 
                        ((Number) row[18]).longValue() > 0,
                        new ArrayList<>() 
                );
                postsMap.put(postId, post);
                commentsMap.put(postId, new ArrayList<>());
            }

            Long userIdComment = ((Number)row[15]).longValue();
            Optional<User> userComment = userRepository.findById(userIdComment);
            Optional<ProfilePhotos> profileOptional = profilePhotosRepository.findByUserId(userIdComment);
            UserDto userCommenDto = null;
            ProfilePhotosDto profilePhotosDto = null;

            if(profileOptional.isPresent()){
                ProfilePhotos profilePhotos = profileOptional.get();
                profilePhotosDto = new ProfilePhotosDto(profilePhotos.getId(), profilePhotos.getUserId(), profilePhotos.getPhotoUrl(), profilePhotos.getWallPhotoUrl());
            }

            if(userComment.isPresent()){
                User user = userComment.get();
                userCommenDto = new UserDto(userIdComment, user.getName(), user.getUserName(), user.getEmail(), user.getPassword(), user.getRole(),profilePhotosDto);
            }
            if (row[14] != null) { 
                CommentsDto comment = new CommentsDto(
                        ((Number) row[14]).longValue(), 
                        postId, 
                        ((Number) row[15]).longValue(), 
                        (String) row[12], 
                        (java.util.Date) row[13], 
                        userCommenDto
                );
                commentsMap.get(postId).add(comment);
            }
        }

        for (Map.Entry<Long, PostsDto> entry : postsMap.entrySet()) {
            entry.getValue().setComments(commentsMap.get(entry.getKey()));
        }

        return new UserProfileDTO(
                ((Number) first[0]).longValue(),
                (String) first[1],
                (String) first[2],
                (String) first[4],
                (String) first[3],
                (String) first[5], 
                (String) first[6], 
                new ArrayList<>(postsMap.values()) 
        );
    }
}
