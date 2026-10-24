package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Friend;
import Proconnect.backProconnect.model.FriendDto;
import Proconnect.backProconnect.repository.FriendRepository;
import Proconnect.backProconnect.exception.ResourceNotFoundException;
import Proconnect.backProconnect.security.OwnershipGuard;

@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final OwnershipGuard guard;

    public FriendService(FriendRepository friendRepository, OwnershipGuard guard) {
        this.friendRepository = friendRepository;
        this.guard = guard;
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
            Friend friend = new Friend(null, userIdFollow, userIdBeFollowed, "PENDING");
            friendRepository.save(friend);
            return "Friend request sent successufly";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Acepta la solicitud {requestId}. Solo su destinatario (o un ADMIN) puede hacerlo: 403 para el resto,
     * 404 si no existe.
     */
    public String AcceptFriendUserIdBeFollowed(Long requestId) {
        Friend friend = findRequestForRecipient(requestId);
        friend.setStatus("ACCEPTED");
        friendRepository.save(friend);
        return "Friend request accepted";
    }

    /** Rechaza la solicitud {requestId}; mismas reglas que aceptar. */
    public String rejectFriendRequest(Long requestId) {
        Friend friend = findRequestForRecipient(requestId);
        friend.setStatus("REJECTED");
        friendRepository.save(friend);
        return "Friend request rejected";
    }

    private Friend findRequestForRecipient(Long requestId) {
        Friend friend = (requestId == null || requestId <= 0) ? null
                : friendRepository.findById(requestId).orElse(null);
        if (friend == null) {
            throw new ResourceNotFoundException();
        }
        guard.assertOwnerOrAdmin(friend.getUserIdBeFollowed());
        return friend;
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
            if(friendOpt.isEmpty()){
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
