package Proconnect.backProconnect.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Comments;
import Proconnect.backProconnect.model.CommentsDto;
import Proconnect.backProconnect.repository.CommentsRepository;
import Proconnect.backProconnect.exception.ResourceNotFoundException;
import Proconnect.backProconnect.security.OwnershipGuard;

@Service
public class CommentsService {
    
    private final CommentsRepository commentsRepository;
    private final OwnershipGuard guard;

    public CommentsService(CommentsRepository commentsRepository, OwnershipGuard guard){
        this.commentsRepository = commentsRepository;
        this.guard = guard;
    }

    public String AddComments(CommentsDto comments){
        try {
            if(comments == null){
                return "Comment cannot be null";
            }
            Comments commentsEntity = new Comments(null, comments.getPostId(), comments.getUserId(), comments.getContent(),
                    comments.getDate() != null ? comments.getDate() : new java.util.Date());
            commentsRepository.save(commentsEntity);
            return "Comment Add successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    /** Solo el autor o un ADMIN (moderación) borran (403); 404 si no existe. */
    public void DeleteCommentById(Long Id){
        if (Id == null || Id <= 0) {
            throw new ResourceNotFoundException();
        }
        Comments comment = commentsRepository.findById(Id).orElseThrow(ResourceNotFoundException::new);
        guard.assertOwnerOrAdmin(comment.getUserId());
        commentsRepository.delete(comment);
    }

    public List<CommentsDto> findByPostId(Long postId){
        try {
            if(postId == null || postId <= 0){
                return null;
            }
            List<Comments> comments = commentsRepository.findByPostId(postId);
            return comments.stream()
                .map(c -> new CommentsDto(c.getId(), c.getPostId(), c.getUserId(), c.getContent(), c.getDate(),null))
                .collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }
}

