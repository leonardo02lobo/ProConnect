package Proconnect.backProconnect.model;

public class ProfilePhotosDto {
    
    private Long Id;
    private Long userId;
    private String photoUrl;
    private String wallPhotoUrl;
    
    public ProfilePhotosDto() {
    }

    public ProfilePhotosDto(Long id, Long userId, String photoUrl, String wallPhotoUrl) {
        Id = id;
        this.userId = userId;
        this.photoUrl = photoUrl;
        this.wallPhotoUrl = wallPhotoUrl;
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
