package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Friend;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findByUserIdFollowAndUserIdBeFollowed(
            Long userIdFollow, Long userIdBeFollowed);

    List<Friend> findByUserIdBeFollowedAndStatusOrderByIdAsc(Long userIdBeFollowed, String status);

    /** Relaciones con el estado dado en las que participa el usuario (en cualquier sentido). */
    @Query(
            "SELECT f FROM Friend f WHERE (f.userIdFollow = :userId OR f.userIdBeFollowed = :userId)"
                    + " AND f.status = :status ORDER BY f.id")
    List<Friend> findByParticipantAndStatus(
            @Param("userId") Long userId, @Param("status") String status);

    /** Relación entre dos usuarios en cualquier sentido. */
    @Query(
            "SELECT f FROM Friend f WHERE (f.userIdFollow = :a AND f.userIdBeFollowed = :b)"
                    + " OR (f.userIdFollow = :b AND f.userIdBeFollowed = :a) ORDER BY f.id")
    List<Friend> findBetween(@Param("a") Long a, @Param("b") Long b);
}
