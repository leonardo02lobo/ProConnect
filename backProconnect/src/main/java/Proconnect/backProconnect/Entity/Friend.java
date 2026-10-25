package Proconnect.backProconnect.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

/** Solicitud/relación de amistad dirigida. UNIQUE(user_id_follow, user_id_be_followed). */
@Entity
@Table(
        name = "Friends",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_friends_pair",
                        columnNames = {"user_id_follow", "user_id_be_followed"}))
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID Follow cannot be null") @Column(name = "user_id_follow", nullable = false)
    private Long userIdFollow;

    /** Relación de solo lectura (LAZY) sobre la FK user_id_follow; se escribe con el campo Long. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id_follow",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_friends_follow"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User follower;

    @NotNull(message = "User ID Be Followed cannot be null") @Column(name = "user_id_be_followed", nullable = false)
    private Long userIdBeFollowed;

    /**
     * Relación de solo lectura (LAZY) sobre la FK user_id_be_followed; se escribe con el campo
     * Long.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id_be_followed",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_friends_followed"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User followed;

    @NotNull(message = "Status cannot be null") @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 16)
    private FriendStatus status;

    /** Requerido por JPA para materializar la entidad. */
    public Friend() {}

    public Friend(Long id, Long userIdFollow, Long userIdBeFollowed, FriendStatus status) {
        this.id = id;
        this.userIdFollow = userIdFollow;
        this.userIdBeFollowed = userIdBeFollowed;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserIdFollow() {
        return userIdFollow;
    }

    public void setUserIdFollow(Long userIdFollow) {
        this.userIdFollow = userIdFollow;
    }

    public User getFollower() {
        return follower;
    }

    public Long getUserIdBeFollowed() {
        return userIdBeFollowed;
    }

    public void setUserIdBeFollowed(Long userIdBeFollowed) {
        this.userIdBeFollowed = userIdBeFollowed;
    }

    public User getFollowed() {
        return followed;
    }

    public FriendStatus getStatus() {
        return status;
    }

    public void setStatus(FriendStatus status) {
        this.status = status;
    }
}
