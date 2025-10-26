package Proconnect.backProconnect.model;

import java.util.Date;
import java.util.List;

public class PostsDto {
    private Long id;
    private Long userId;
    private String content;
    private String title;
    private String imageUrl;
    private Date date;
    private Long totalLikes;
    private Long totalComentarios;
    private boolean userLiked;
    private List<CommentsDto> comments;

    public PostsDto() {
    }

    public PostsDto(Long id, Long userId, String content, String title, String imageUrl, Date date, Long totalLikes,
            Long totalComentarios, boolean userLiked, List<CommentsDto> comments) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.title = title;
        this.imageUrl = imageUrl;
        this.date = date;
        this.totalLikes = totalLikes;
        this.totalComentarios = totalComentarios;
        this.userLiked = userLiked;
        this.comments = comments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(Long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public Long getTotalComentarios() {
        return totalComentarios;
    }

    public void setTotalComentarios(Long totalComentarios) {
        this.totalComentarios = totalComentarios;
    }

    public boolean isUserLiked() {
        return userLiked;
    }

    public void setUserLiked(boolean userLiked) {
        this.userLiked = userLiked;
    }

    public List<CommentsDto> getComments() {
        return comments;
    }

    public void setComments(List<CommentsDto> comments) {
        this.comments = comments;
    }
}