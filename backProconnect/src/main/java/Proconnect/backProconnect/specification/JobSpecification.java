package Proconnect.backProconnect.specification;

import org.springframework.data.jpa.domain.Specification;

import Proconnect.backProconnect.Entity.Job;

public class JobSpecification {
    
    public static Specification<Job> hasKeyWord(String keyWord){
        return (root,query,criteriaBuilder) -> {
            if(keyWord == null || keyWord.trim().isEmpty()){
                return criteriaBuilder.conjunction();
            }
            String pattern = "%"+keyWord.toLowerCase()+"%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }
}
