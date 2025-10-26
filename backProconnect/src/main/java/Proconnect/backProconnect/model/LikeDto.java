package Proconnect.backProconnect.model;

import java.util.Date;

public class LikeDto {
    
    private Long Id;
    private Long userId;
    private Long postId;
    private Date date;

    public LikeDto(){

    }

    public LikeDto(Long id, Long userId, Long postId, Date date) {
        Id = id;
        this.userId = userId;
        this.postId = postId;
        this.date = date;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    
}
