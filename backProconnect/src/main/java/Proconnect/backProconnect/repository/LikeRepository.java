package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.models.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long>{
    
}