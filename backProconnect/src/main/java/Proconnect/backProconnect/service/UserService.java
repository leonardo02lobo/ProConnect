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
import Proconnect.backProconnect.model.UserDto;
import Proconnect.backProconnect.model.UserProfileDTO;
import Proconnect.backProconnect.model.UserPublicDto;
import Proconnect.backProconnect.repository.ProfilePhotosRepository;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.exception.ResourceNotFoundException;
import Proconnect.backProconnect.security.OwnershipGuard;
import Proconnect.backProconnect.specification.UserSpecification;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfilePhotosRepository profilePhotosRepository;
    private final OwnershipGuard guard;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            ProfilePhotosRepository profilePhotosRepository, OwnershipGuard guard) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profilePhotosRepository = profilePhotosRepository;
        this.guard = guard;
    }

    /** Vista pública: el email solo se incluye si el actor es el propio usuario o un ADMIN. */
    public UserPublicDto toPublic(User user) {
        return user == null ? null : toPublic(user, guard.canSeePrivateDataOf(user.getId()));
    }

    /** Vista del propio usuario (login, registro, /me): incluye su email. */
    public UserPublicDto toSelf(User user) {
        return user == null ? null : toPublic(user, true);
    }

    public UserPublicDto getSelfById(Long id) {
        return id == null ? null : userRepository.findById(id).map(this::toSelf).orElse(null);
    }

    private UserPublicDto toPublic(User user, boolean includePrivate) {
        Optional<ProfilePhotos> photos = profilePhotosRepository.findByUserId(user.getId());
        return new UserPublicDto(user.getId(), user.getName(), user.getUserName(),
                includePrivate ? user.getEmail() : null, user.getRole(),
                photos.map(ProfilePhotos::getPhotoUrl).orElse(null),
                photos.map(ProfilePhotos::getWallPhotoUrl).orElse(null));
    }

    public UserPublicDto getPublicById(Long id) {
        if (id == null) {
            return null;
        }
        return userRepository.findById(id).map(this::toPublic).orElse(null);
    }

    public List<UserPublicDto> GetAllUsers() {
        return userRepository.findAll().stream().map(this::toPublic).toList();
    }

    /** Solo el propio usuario o un ADMIN (403); 404 si no existe. */
    public String UpdateUserById(Long Id, UserDto newUser) {
        User existingUser = (Id == null ? null : userRepository.findById(Id).orElse(null));
        if (existingUser == null) {
            throw new ResourceNotFoundException();
        }
        guard.assertOwnerOrAdmin(existingUser.getId());
        if (newUser == null) {
            return "Body requerido";
        }
        try {
            // El id y el rol no se cambian desde aquí; el password se guarda siempre hasheado.
            existingUser.setName(newUser.getName());
            existingUser.setEmail(newUser.getEmail());
            if (newUser.getPassword() != null && !newUser.getPassword().isBlank()) {
                if (newUser.getPassword().length() < AuthService.MIN_PASSWORD) {
                    return "Password must be at least " + AuthService.MIN_PASSWORD + " characters";
                }
                existingUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            }
            existingUser.setUserName(newUser.getUserName());
            userRepository.save(existingUser);
            return "User with Id " + Id + " updated successfully";
        } catch (Exception e) {
            // Sin detalles de la BD en la respuesta.
            return "Error: no se pudo actualizar el usuario";
        }
    }

    /** Solo el propio usuario o un ADMIN (403); false si no existe. */
    public boolean DeleteUserById(Long Id) {
        User existing = (Id == null ? null : userRepository.findById(Id).orElse(null));
        if (existing == null) {
            return false;
        }
        guard.assertOwnerOrAdmin(existing.getId());
        userRepository.deleteById(Id);
        return true;
    }

    public UserPublicDto GetUserById(Long Id) {
        return getPublicById(Id);
    }

    public List<UserPublicDto> SearchByNameOrUsername(String name) {
        return userRepository.findAll(UserSpecification.hasNameLike(name)).stream().map(this::toPublic).toList();
    }

    public UserProfileDTO findUsersWithProfilesAndPosts(Long userId) {
        List<Object[]> results = userRepository.findUsersWithProfilesAndPosts(userId);
        if (results.isEmpty())
            return null;

        Object[] first = results.get(0);

        Map<Long, PostsDto> postsMap = new HashMap<>();
        Map<Long, List<CommentsDto>> commentsMap = new HashMap<>();

        for (Object[] row : results) {
            if (row[7] == null) {
                continue; // usuario sin posts
            }
            Long postId = ((Number) row[7]).longValue();

            if (!postsMap.containsKey(postId)) {
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

            if (row[14] != null && row[15] != null) {
                Long userIdComment = ((Number) row[15]).longValue();
                CommentsDto comment = new CommentsDto(
                        ((Number) row[14]).longValue(),
                        postId,
                        userIdComment,
                        (String) row[12],
                        (java.util.Date) row[13],
                        getPublicById(userIdComment)
                );
                commentsMap.get(postId).add(comment);
            }
        }

        for (Map.Entry<Long, PostsDto> entry : postsMap.entrySet()) {
            entry.getValue().setComments(commentsMap.get(entry.getKey()));
        }

        return new UserProfileDTO(
                ((Number) first[0]).longValue(),
                guard.canSeePrivateDataOf(((Number) first[0]).longValue()) ? (String) first[1] : null,
                (String) first[2],
                (String) first[4],
                (String) first[3],
                (String) first[5], 
                (String) first[6], 
                new ArrayList<>(postsMap.values()) 
        );
    }
}
