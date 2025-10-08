package Proconnect.backProconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.model.CommentsDto;

@Repository
public interface CommentsRepository extends JpaRepository<CommentsDto,Long>{
    List<CommentsDto> findByPostId(Long postId);
}
