package Proconnect.backProconnect.model;

import java.util.Date;

public class CommentsDto {

    private Long Id;
    private Long postId;
    private Long userId;
    private String content;
    private Date date;

    public CommentsDto() {
    }

    public CommentsDto(Long id, Long postId, Long userId, String content, Date date) {
        Id = id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.date = date;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
