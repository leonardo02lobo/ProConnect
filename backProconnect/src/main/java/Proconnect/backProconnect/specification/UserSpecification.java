package Proconnect.backProconnect.specification;

import Proconnect.backProconnect.Entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {

    private UserSpecification() {}

    /** Coincidencia parcial, sin distinguir mayúsculas, en el nombre o en el userName. */
    public static Specification<User> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + name.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("userName")), pattern));
        };
    }

    /**
     * Búsqueda de administración: coincidencia parcial, sin distinguir mayúsculas, en nombre,
     * userName o email. Solo la usan rutas de ADMIN (el email no es buscable por terceros).
     */
    public static Specification<User> matchesAdminQuery(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + escapeLike(q.trim().toLowerCase()) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("userName")), pattern, '\\'),
                    cb.like(cb.lower(root.get("email")), pattern, '\\'));
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
