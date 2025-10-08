package Proconnect.backProconnect.specification;

import org.springframework.data.jpa.domain.Specification;

import Proconnect.backProconnect.Entity.User;

public class UserSpecification {

    public static Specification<User> hasNameLike(String name) {
        return (root, query, criteriBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return criteriBuilder.conjunction();
            }
            return criteriBuilder.like(criteriBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%");
        };
    }
}
