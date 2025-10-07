package Proconnect.backProconnect.specification;

import org.springframework.data.jpa.domain.Specification;

import Proconnect.backProconnect.model.UserDto;

public class UserSpecification {

    public static Specification<UserDto> hasNameLike(String name) {
        return (root, query, criteriBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return criteriBuilder.conjunction();
            }
            return criteriBuilder.like(criteriBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%");
        };
    }
}
