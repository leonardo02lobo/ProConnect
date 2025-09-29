package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.models.Comments;

@Repository
public interface CommentsRepository extends JpaRepository<Comments,Long>{
    
}
