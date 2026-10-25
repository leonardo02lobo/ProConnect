package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);

    boolean existsByUserName(String userName);

    long countByRole(String role);

    /** Rol vigente (RoleResolver): solo la columna, sin cargar la entidad. */
    @Query("select u.role from User u where u.id = :id")
    Optional<String> findRoleById(@Param("id") Long id);

    /** SELECT ... FOR UPDATE del usuario objetivo de un cambio de rol. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    /**
     * SELECT ... FOR UPDATE de todos los usuarios con el rol indicado, en orden de id (orden fijo
     * para evitar interbloqueos): serializa los cambios de rol concurrentes que afectan a ADMINs.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.role = :role order by u.id")
    List<User> findAllByRoleForUpdate(@Param("role") String role);
}
