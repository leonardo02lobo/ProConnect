package Proconnect.backProconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.model.FriendDto;

@Repository
public interface FriendRepository extends JpaRepository<FriendDto,Long>{
    Optional<FriendDto> findByUserIdFollowAndUserIdBeFollowed(Long userIdFollow, Long userIdBeFollowed);
    List<FriendDto> findByUserIdBeFollowedAndStatus(Long userIdBeFollowed,String status);
    List<FriendDto> findByUserIdFollowAndStatus(Long userIdFollow,String status);
    List<FriendDto> findByUserIdFollowOrUserIdBeFollowedAndStatus(Long userIdFollow, Long userIdBeFollowed, String status);
}