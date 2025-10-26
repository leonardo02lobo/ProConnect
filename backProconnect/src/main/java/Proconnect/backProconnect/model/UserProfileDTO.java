package Proconnect.backProconnect.model;

import java.util.List;

public class UserProfileDTO {
    private Long id;
    private String email;
    private String name;
    private String role;
    private String userName;
    private String photoUrl;
    private String wallPhotoUrl;
    private List<PostsDto> posts;

    public UserProfileDTO() {
    }

    public UserProfileDTO(Long id, String email, String name, String role, String userName,
            String photoUrl, String wallPhotoUrl, List<PostsDto> posts) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.userName = userName;
        this.photoUrl = photoUrl;
        this.wallPhotoUrl = wallPhotoUrl;
        this.posts = posts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public List<PostsDto> getPosts() {
        return posts;
    }

    public void setPosts(List<PostsDto> posts) {
        this.posts = posts;
    }
}