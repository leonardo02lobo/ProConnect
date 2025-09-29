package Proconnect.backProconnect.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Friends")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID Follow cannot be null")
    private Long userIdFollow;

    @NotNull(message = "User ID Be Followed cannot be null")
    private Long userIdBeFollowed;

    @NotBlank(message = "Status is mandatory")
    @NotNull(message = "Status cannot be null")
    private String status;

    public Friend(Long id, Long userIdFollow, Long userIdBeFollowed, String status) {
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

    public Long getUserIdBeFollowed() {
        return userIdBeFollowed;
    }

    public void setUserIdBeFollowed(Long userIdBeFollowed) {
        this.userIdBeFollowed = userIdBeFollowed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
