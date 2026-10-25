package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.Role;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);

    Slice<User> findAllBy(Pageable pageable);

    /** Coincidencia parcial, sin distinguir mayúsculas, en el nombre o en el userName. */
    @Query(
            "SELECT u FROM User u WHERE LOWER(u.name) LIKE :pattern OR LOWER(u.userName) LIKE"
                    + " :pattern")
    Slice<User> search(@Param("pattern") String pattern, Pageable pageable);

    /**
     * Búsqueda de administración: coincidencia parcial, sin distinguir mayúsculas, en nombre,
     * userName o email. {@code pattern} llega ya en minúsculas, con los comodines LIKE escapados
     * con {@code \} y envuelto en {@code %} (ver {@code RoleService.listUsers}); {@code null} = sin
     * filtro. Devuelve {@link Page} porque la UI de administración muestra el total.
     */
    @Query(
            value =
                    "SELECT u FROM User u WHERE :pattern IS NULL"
                            + " OR LOWER(u.name) LIKE :pattern ESCAPE '\\'"
                            + " OR LOWER(u.userName) LIKE :pattern ESCAPE '\\'"
                            + " OR LOWER(u.email) LIKE :pattern ESCAPE '\\'",
            countQuery =
                    "SELECT COUNT(u) FROM User u WHERE :pattern IS NULL"
                            + " OR LOWER(u.name) LIKE :pattern ESCAPE '\\'"
                            + " OR LOWER(u.userName) LIKE :pattern ESCAPE '\\'"
                            + " OR LOWER(u.email) LIKE :pattern ESCAPE '\\'")
    Page<User> adminSearch(@Param("pattern") String pattern, Pageable pageable);

    boolean existsByUserName(String userName);

    long countByRole(Role role);

    /** Rol vigente (RoleResolver): solo la columna, sin cargar la entidad. */
    @Query("select u.role from User u where u.id = :id")
    Optional<Role> findRoleById(@Param("id") Long id);

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
    List<User> findAllByRoleForUpdate(@Param("role") Role role);
}
