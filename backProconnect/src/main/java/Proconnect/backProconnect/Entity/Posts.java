package Proconnect.backProconnect.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Post. La FK {@code user_id} es la fuente de verdad (campo {@code userId}, el que se escribe); la
 * relación {@code author} es de solo lectura y LAZY, para navegar en JPQL ({@code JOIN p.author})
 * sin cargas accidentales. Sin colecciones {@code @OneToMany} (design D3).
 */
@Entity
@Table(name = "Posts")
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID cannot be null") @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_posts_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User author;

    @NotBlank(message = "Content is mandatory") @Column(nullable = false)
    private String content;

    @NotBlank(message = "Title is mandatory") @Column(nullable = false)
    private String title;

    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Posts() {}

    public Posts(
            Long id,
            Long userId,
            String content,
            String title,
            String imageUrl,
            Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.title = title;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
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

    public User getAuthor() {
        return author;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
