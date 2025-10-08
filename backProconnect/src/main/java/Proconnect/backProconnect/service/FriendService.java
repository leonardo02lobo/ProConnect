package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Friend;
import Proconnect.backProconnect.model.FriendDto;
import Proconnect.backProconnect.repository.FriendRepository;

@Service
public class FriendService {

    private final FriendRepository friendRepository;

    public FriendService(FriendRepository friendRepository) {
        this.friendRepository = friendRepository;
    }

    public String sendFriendRequest(Long userIdFollow, Long userIdBeFollowed) {
        try {
            if (userIdFollow == null || userIdFollow <= 0) {
                return "userIdFollow cannot be null or less zero";
            }
            if (userIdBeFollowed == null || userIdBeFollowed <= 0) {
                return "userIdBeFollowed cannot be null or less zero";
            }
            if (userIdFollow.equals(userIdBeFollowed)) {
                return "You cannot send a friend request to yourself.";
            }
            Optional<Friend> existing = friendRepository.findByUserIdFollowAndUserIdBeFollowed(userIdFollow,
                    userIdBeFollowed);
            if (existing.isPresent()) {
                return "Friend request already exists";
            }
            Friend friend = new Friend(Long.valueOf(0),userIdFollow, userIdBeFollowed, "PENDING");
            friendRepository.save(friend);
            return "Friend request sent successufly";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String AcceptFriendUserIdBeFollowed(Long userIdBeFollowed) {
        try {
            if (userIdBeFollowed == null || userIdBeFollowed <= 0) {
                return "UserIdBeFollowed cannot be null or less zero";
            }
            Optional<Friend> friendoOpt = friendRepository.findById(userIdBeFollowed);
            if (friendoOpt.isPresent()) {
                return "Friend request not found";
            }
            Friend friend = friendoOpt.get();
            friend.setStatus("ACCEPTED");
            friendRepository.save(friend);
            return "Friend request accepted";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String rejectFriendRequest(Long userIdBeFollowed) {
        try {
            Optional<Friend> friendOpt = friendRepository.findById(userIdBeFollowed);
            if (friendOpt.isPresent()) {
                return "Friend request not found";
            }
            Friend friend = friendOpt.get();
            friend.setStatus("REJECTED");
            friendRepository.save(friend);
            return "Friend request rejected";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String deleteFriendShip(Long userIdFollow, Long userIdBeFollowed){
        try {
            if (userIdFollow == null || userIdFollow <= 0) {
                return "userIdFollow cannot be null or less zero";
            }
            if (userIdBeFollowed == null || userIdBeFollowed <= 0) {
                return "userIdBeFollowed cannot be null or less zero";
            }
            if (userIdFollow.equals(userIdBeFollowed)) {
                return "You cannot send a friend request to yourself.";
            }
            List<Friend> relations = friendRepository.findByUserIdFollowOrUserIdBeFollowedAndStatus(userIdFollow,userIdBeFollowed,"ACCEPTED");
            if(relations.isEmpty()){
                return "FriendShip not found";
            }
            friendRepository.deleteAll(relations);
            return "FriendShip deleted successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public String cancelFriendUserIdBeFollowed(Long userIdFollow, Long userIdBeFollowed){
        try {
            if (userIdFollow == null || userIdFollow <= 0) {
                return "userIdFollow cannot be null or less zero";
            }
            if (userIdBeFollowed == null || userIdBeFollowed <= 0) {
                return "userIdBeFollowed cannot be null or less zero";
            }
            if (userIdFollow.equals(userIdBeFollowed)) {
                return "You cannot send a friend request to yourself.";
            }
            Optional<Friend> friendOpt = friendRepository.findByUserIdFollowAndUserIdBeFollowed(userIdFollow, userIdBeFollowed);
            if(friendOpt.isPresent()){
                return "Friend request not found";
            }
            Friend friend = friendOpt.get();
            if(!friend.getStatus().equals("PENDING")){
                return "Cannot cancel a request that is not pending";
            }

            friendRepository.delete(friend);
            return "Friend request cancelled";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public List<FriendDto> getPendingRequests(Long userId) {
        return friendRepository.findByUserIdBeFollowedAndStatus(userId, "PENDING");
    }

    public List<Friend> getFriends(Long userId) {
        return friendRepository.findByUserIdFollowOrUserIdBeFollowedAndStatus(userId, userId, "ACCEPTED");
    }

    public Optional<Friend> findFriendship(Long userId, Long otherUserId) {
        return friendRepository.findByUserIdFollowAndUserIdBeFollowed(userId, otherUserId);
    }
}
