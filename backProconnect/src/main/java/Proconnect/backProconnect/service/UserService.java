package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.ProfilePhotos;
import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.exception.ConflictException;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.UserProfileDto;
import Proconnect.backProconnect.model.UserPublicDto;
import Proconnect.backProconnect.model.UserUpdateRequest;
import Proconnect.backProconnect.repository.ProfilePhotosRepository;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.security.OwnershipGuard;
import Proconnect.backProconnect.specification.UserSpecification;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfilePhotosRepository profilePhotosRepository;
    private final PostsService postsService;
    private final OwnershipGuard guard;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ProfilePhotosRepository profilePhotosRepository,
            PostsService postsService,
            OwnershipGuard guard) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profilePhotosRepository = profilePhotosRepository;
        this.postsService = postsService;
        this.guard = guard;
    }

    /** Vista pública: el email solo se incluye si el actor es el propio usuario o un ADMIN. */
    public UserPublicDto toPublic(User user) {
        return toPublic(user, guard.canSeePrivateDataOf(user.getId()));
    }

    /** Vista del propio usuario (login, registro, /me): incluye su email. */
    public UserPublicDto toSelf(User user) {
        return toPublic(user, true);
    }

    /** /me: vacío si el usuario del token ya no existe. */
    @Transactional(readOnly = true)
    public Optional<UserPublicDto> findSelf(Long id) {
        return id == null ? Optional.empty() : userRepository.findById(id).map(this::toSelf);
    }

    @Transactional(readOnly = true)
    public List<UserPublicDto> listUsers(Optional<String> query) {
        List<User> users =
                query.filter(q -> !q.isBlank())
                        .map(q -> userRepository.findAll(UserSpecification.hasNameLike(q.trim())))
                        .orElseGet(userRepository::findAll);
        return users.stream().map(this::toPublic).toList();
    }

    @Transactional(readOnly = true)
    public UserPublicDto getUser(Long id) {
        return toPublic(findOr404(id));
    }

    /**
     * Solo el propio usuario o un ADMIN (403); 404 si no existe; 409 si email/userName son de otro
     * usuario.
     */
    @Transactional
    public UserPublicDto updateUser(Long id, UserUpdateRequest request) {
        User user = findOr404(id);
        guard.assertOwnerOrAdmin(user.getId());
        String email = request.email().trim();
        String userName = request.userName().trim();
        userRepository
                .findByEmail(email)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(
                        other -> {
                            throw new ConflictException("El email ya está registrado");
                        });
        userRepository
                .findByUserName(userName)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(
                        other -> {
                            throw new ConflictException("El userName ya está registrado");
                        });
        // El id y el rol no se cambian desde aquí; el password se guarda siempre hasheado.
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setUserName(userName);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return toPublic(userRepository.save(user));
    }

    /** Solo el propio usuario o un ADMIN (403); 404 si no existe. */
    @Transactional
    public void deleteUser(Long id) {
        User user = findOr404(id);
        guard.assertOwnerOrAdmin(user.getId());
        userRepository.delete(user);
    }

    /**
     * Perfil con sus posts; {@code userLiked} de cada post es del viewer (el usuario de la sesión).
     */
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long id, Optional<Long> viewerId) {
        User user = findOr404(id);
        Optional<ProfilePhotos> photos =
                profilePhotosRepository.findFirstByUserIdOrderByIdDesc(user.getId());
        return new UserProfileDto(
                user.getId(),
                guard.canSeePrivateDataOf(user.getId()) ? user.getEmail() : null,
                user.getName(),
                user.getRole().name(),
                user.getUserName(),
                photos.map(ProfilePhotos::getPhotoUrl).orElse(null),
                photos.map(ProfilePhotos::getWallPhotoUrl).orElse(null),
                postsService.getPostsByAuthor(user.getId(), viewerId));
    }

    User findOr404(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException("Usuario no encontrado");
        }
        return userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private UserPublicDto toPublic(User user, boolean includePrivate) {
        Optional<ProfilePhotos> photos =
                profilePhotosRepository.findFirstByUserIdOrderByIdDesc(user.getId());
        return new UserPublicDto(
                user.getId(),
                user.getName(),
                user.getUserName(),
                includePrivate ? user.getEmail() : null,
                user.getRole().name(),
                photos.map(ProfilePhotos::getPhotoUrl).orElse(null),
                photos.map(ProfilePhotos::getWallPhotoUrl).orElse(null));
    }
}
