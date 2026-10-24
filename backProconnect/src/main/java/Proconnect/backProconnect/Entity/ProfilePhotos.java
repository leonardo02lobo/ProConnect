package Proconnect.backProconnect.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ProfilePhotos")
public class ProfilePhotos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    private String photoUrl;

    private String wallPhotoUrl;

    public ProfilePhotos() {
    }

    public ProfilePhotos(Long id, Long userId, String photoUrl, String wallPhotoUrl) {
        this.id = id;
        this.userId = userId;
        this.photoUrl = photoUrl;
        this.wallPhotoUrl = wallPhotoUrl;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getWallPhotoUrl() {
        return wallPhotoUrl;
    }

    public void setWallPhotoUrl(String wallPhotoUrl) {
        this.wallPhotoUrl = wallPhotoUrl;
    }

}
