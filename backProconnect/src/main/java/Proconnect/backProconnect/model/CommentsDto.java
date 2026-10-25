package Proconnect.backProconnect.model;

import java.util.Date;

public class CommentsDto {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Date date;
    private UserPublicDto user;

    public CommentsDto() {
    }

    public CommentsDto(Long id, Long postId, Long userId, String content, Date date, UserPublicDto user) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.date = date;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public UserPublicDto getUser() {
        return user;
    }

    public void setUser(UserPublicDto user) {
        this.user = user;
    }
}