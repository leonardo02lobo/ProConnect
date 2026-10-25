package Proconnect.backProconnect.specification;

import Proconnect.backProconnect.Entity.WorkStation;
import org.springframework.data.jpa.domain.Specification;

public class WorkStationSpecification {

    public static Specification<WorkStation> hasWorkStationLike(String workStation) {
        return (root, query, criteriBuilder) -> {
            if (workStation == null || workStation.trim().isEmpty()) {
                return criteriBuilder.conjunction();
            }
            return criteriBuilder.like(
                    criteriBuilder.lower(root.get("workStation")),
                    "%" + workStation.toLowerCase() + "%");
        };
    }
}
