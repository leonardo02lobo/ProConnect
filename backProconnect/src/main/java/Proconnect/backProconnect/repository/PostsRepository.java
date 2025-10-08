package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.model.PostsDto;

@Repository
public interface PostsRepository extends JpaRepository<PostsDto, Long>{
    
}