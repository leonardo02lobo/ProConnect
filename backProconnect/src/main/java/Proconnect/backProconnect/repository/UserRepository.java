package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.User;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
