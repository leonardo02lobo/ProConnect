package Proconnect.backProconnect.model;

import java.util.Date;

public class PostsDto {
    private Long Id;
    private Long userId;
    private String content;
    private String Title;
    private String imageUrl;
    private Date date;

    public PostsDto() {

    }

    public PostsDto(Long id, Long userId, String content, String title, String imageUrl, Date date) {
        Id = id;
        this.userId = userId;
        this.content = content;
        Title = title;
        this.imageUrl = imageUrl;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
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

}
