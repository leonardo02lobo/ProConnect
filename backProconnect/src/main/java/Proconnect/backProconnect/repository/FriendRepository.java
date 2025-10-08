package Proconnect.backProconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Friend;
import Proconnect.backProconnect.model.FriendDto;

@Repository
public interface FriendRepository extends JpaRepository<Friend,Long>{
    Optional<Friend> findByUserIdFollowAndUserIdBeFollowed(Long userIdFollow, Long userIdBeFollowed);
    List<FriendDto> findByUserIdBeFollowedAndStatus(Long userIdBeFollowed,String status);
    List<FriendDto> findByUserIdFollowAndStatus(Long userIdFollow,String status);
    List<Friend> findByUserIdFollowOrUserIdBeFollowedAndStatus(Long userIdFollow, Long userIdBeFollowed, String status);
}