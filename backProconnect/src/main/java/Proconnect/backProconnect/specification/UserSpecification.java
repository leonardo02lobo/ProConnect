package Proconnect.backProconnect.specification;

import org.springframework.data.jpa.domain.Specification;

import Proconnect.backProconnect.Entity.User;

public final class UserSpecification {

    private UserSpecification() {
    }

    /** Coincidencia parcial, sin distinguir mayúsculas, en el nombre o en el userName. */
    public static Specification<User> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + name.trim().toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("userName")), pattern));
        };
    }
}
