package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Friend;
import Proconnect.backProconnect.Entity.FriendStatus;
import Proconnect.backProconnect.exception.ConflictException;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.exception.ValidationException;
import Proconnect.backProconnect.model.FriendRequestDto;
import Proconnect.backProconnect.repository.FriendRepository;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.security.OwnershipGuard;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FriendService {

    static final FriendStatus PENDING = FriendStatus.PENDING;
    static final FriendStatus ACCEPTED = FriendStatus.ACCEPTED;
    static final FriendStatus REJECTED = FriendStatus.REJECTED;

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final OwnershipGuard guard;

    public FriendService(
            FriendRepository friendRepository,
            UserRepository userRepository,
            OwnershipGuard guard) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.guard = guard;
    }

    /**
     * Solicitud de {@code requesterId} (sesión) a {@code recipientId}: 400 a uno mismo, 404 si el
     * destinatario no existe y 409 si ya hay una solicitud pendiente o una amistad entre ambos. Una
     * solicitud rechazada se puede volver a enviar.
     */
    @Transactional
    public FriendRequestDto sendRequest(Long requesterId, Long recipientId) {
        if (requesterId.equals(recipientId)) {
            throw new ValidationException("userId", "No puedes enviarte una solicitud a ti mismo");
        }
        if (!userRepository.existsById(recipientId)) {
            throw new NotFoundException("Usuario no encontrado");
        }
        List<Friend> existing = friendRepository.findBetween(requesterId, recipientId);
        if (existing.stream().anyMatch(f -> !REJECTED.equals(f.getStatus()))) {
            throw new ConflictException("Ya existe una solicitud o amistad con este usuario");
        }
        friendRepository.deleteAll(existing);
        return toDto(friendRepository.save(new Friend(null, requesterId, recipientId, PENDING)));
    }

    /** Solicitudes pendientes recibidas por el usuario. */
    @Transactional(readOnly = true)
    public List<FriendRequestDto> pendingReceivedBy(Long userId) {
        return friendRepository
                .findByUserIdBeFollowedAndStatusOrderByIdAsc(userId, PENDING)
                .stream()
                .map(FriendService::toDto)
                .toList();
    }

    /**
     * Solo el destinatario (o un ADMIN) acepta: 403 al resto, 404 si no existe, 409 si ya no está
     * pendiente.
     */
    @Transactional
    public FriendRequestDto accept(Long requestId) {
        return resolve(requestId, ACCEPTED);
    }

    /** Rechaza la solicitud; mismas reglas que aceptar. */
    @Transactional
    public FriendRequestDto reject(Long requestId) {
        return resolve(requestId, REJECTED);
    }

    /**
     * El emisor (o un ADMIN) cancela su solicitud pendiente: 403 al resto, 409 si ya se resolvió.
     */
    @Transactional
    public void cancel(Long requestId) {
        Friend friend = findOr404(requestId);
        guard.assertOwnerOrAdmin(friend.getUserIdFollow());
        if (!PENDING.equals(friend.getStatus())) {
            throw new ConflictException("Solo se pueden cancelar solicitudes pendientes");
        }
        friendRepository.delete(friend);
    }

    /** Amistades aceptadas del usuario. */
    @Transactional(readOnly = true)
    public List<FriendRequestDto> friendsOf(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Usuario no encontrado");
        }
        return friendRepository.findByParticipantAndStatus(userId, ACCEPTED).stream()
                .map(FriendService::toDto)
                .toList();
    }

    /**
     * Relación (pendiente, aceptada o rechazada) entre el usuario de la sesión y otro: 404 si no
     * hay ninguna.
     */
    @Transactional(readOnly = true)
    public FriendRequestDto relationBetween(Long me, Long otherUserId) {
        return friendRepository.findBetween(me, otherUserId).stream()
                .reduce((first, last) -> last)
                .map(FriendService::toDto)
                .orElseThrow(() -> new NotFoundException("No hay relación"));
    }

    /** Elimina la amistad aceptada entre el usuario de la sesión y otro: 404 si no son amigos. */
    @Transactional
    public void removeFriendship(Long me, Long otherUserId) {
        List<Friend> accepted =
                friendRepository.findBetween(me, otherUserId).stream()
                        .filter(f -> ACCEPTED.equals(f.getStatus()))
                        .toList();
        if (accepted.isEmpty()) {
            throw new NotFoundException("No sois amigos");
        }
        friendRepository.deleteAll(accepted);
    }

    private FriendRequestDto resolve(Long requestId, FriendStatus status) {
        Friend friend = findOr404(requestId);
        guard.assertOwnerOrAdmin(friend.getUserIdBeFollowed());
        if (!PENDING.equals(friend.getStatus())) {
            throw new ConflictException("La solicitud ya fue resuelta");
        }
        friend.setStatus(status);
        return toDto(friendRepository.save(friend));
    }

    private Friend findOr404(Long requestId) {
        if (requestId == null || requestId <= 0) {
            throw new NotFoundException("Solicitud no encontrada");
        }
        return friendRepository
                .findById(requestId)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));
    }

    private static FriendRequestDto toDto(Friend f) {
        return new FriendRequestDto(
                f.getId(), f.getUserIdFollow(), f.getUserIdBeFollowed(), f.getStatus().name());
    }
}
