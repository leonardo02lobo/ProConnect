package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Posts;

@Repository
public interface PostsRepository extends JpaRepository<Posts, Long>{
    
}