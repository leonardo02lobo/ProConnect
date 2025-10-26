package Proconnect.backProconnect.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Comments;
import Proconnect.backProconnect.model.CommentsDto;
import Proconnect.backProconnect.repository.CommentsRepository;

@Service
public class CommentsService {
    
    private final CommentsRepository commentsRepository;

    public CommentsService(CommentsRepository commentsRepository){
        this.commentsRepository = commentsRepository;
    }

    public String AddComments(CommentsDto comments){
        try {
            if(comments == null){
                return "Comment cannot be null";
            }
            Comments commentsEntity = new Comments(comments.getId(), comments.getPostId(), comments.getUserId(), comments.getContent(), comments.getDate());
            commentsRepository.save(commentsEntity);
            return "Comment Add successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public String DeleteCommentById(Long Id){
        try {
            if(Id == null || Id <= 0){
                return "Id cannot be null or less zero";
            }
            commentsRepository.deleteById(Id);
            return "Id delete successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
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

