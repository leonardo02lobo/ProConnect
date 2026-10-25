package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.RoleChange;
import java.util.List;
import org.springframework.data.repository.Repository;

/**
 * Auditoría de roles: solo inserción y lectura. Extiende {@link Repository} (no JpaRepository) para
 * no exponer update/delete.
 */
public interface RoleChangeRepository extends Repository<RoleChange, Long> {

    RoleChange save(RoleChange change);

    List<RoleChange> findByTargetUserIdOrderByIdAsc(Long targetUserId);

    long count();
}
