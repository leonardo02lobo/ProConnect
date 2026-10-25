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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** Like. UNIQUE(user_id, post_id): la base impide likes duplicados (409 en la API). */
@Entity
@Table(
        name = "Likes",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_likes_user_post",
                        columnNames = {"user_id", "post_id"}))
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID cannot be null") @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Relación de solo lectura (LAZY) sobre la FK user_id; se escribe con el campo Long. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_likes_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @NotNull(message = "Post ID cannot be null") @Column(name = "post_id", nullable = false)
    private Long postId;

    /** Relación de solo lectura (LAZY) sobre la FK post_id; se escribe con el campo Long. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "post_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_likes_post"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Posts post;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Like() {}

    public Like(Long id, Long userId, Long postId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
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

    public User getUser() {
        return user;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Posts getPost() {
        return post;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
