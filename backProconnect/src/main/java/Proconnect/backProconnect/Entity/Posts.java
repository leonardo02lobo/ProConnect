package Proconnect.backProconnect.Entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Posts")
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "Content is mandatory")
    @NotNull(message = "Content cannot be null")
    private String content;

    @NotBlank(message = "Title is mandatory")
    @NotNull(message = "Title cannot be null")
    private String Title;

    private String imageUrl;

    private Date date;

    public Posts(Long id, Long userId, String content, String title, String imageUrl, Date date) {
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
