package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Friend;
import Proconnect.backProconnect.Entity.FriendStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /** Solicitudes recibidas con un estado (índice idx_friends_followed_status). */
    Slice<Friend> findByUserIdBeFollowedAndStatus(
            Long userIdBeFollowed, FriendStatus status, Pageable pageable);

    /** Relaciones con el estado dado en las que participa el usuario (en cualquier sentido). */
    @Query(
            "SELECT f FROM Friend f WHERE (f.userIdFollow = :userId OR f.userIdBeFollowed ="
                    + " :userId) AND f.status = :status")
    Slice<Friend> findByParticipantAndStatus(
            @Param("userId") Long userId, @Param("status") FriendStatus status, Pageable pageable);

    /** Relación entre dos usuarios en cualquier sentido (uk_friends_pair en ambos sentidos). */
    @Query(
            "SELECT f FROM Friend f WHERE (f.userIdFollow = :a AND f.userIdBeFollowed = :b)"
                    + " OR (f.userIdFollow = :b AND f.userIdBeFollowed = :a) ORDER BY f.id")
    List<Friend> findBetween(@Param("a") Long a, @Param("b") Long b);
}
