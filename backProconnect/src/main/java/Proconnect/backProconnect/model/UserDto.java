package Proconnect.backProconnect.model;

public class UserDto {
    
    private Long id;
    private String name;
    private String userName;
    private String email;
    private String password;
    private String role;
    private ProfilePhotosDto profilePhotosDto;

    public UserDto() {
    }

    public UserDto(Long id, String name, String userName, String email, String password, String role,
            ProfilePhotosDto profilePhotosDto) {
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profilePhotosDto = profilePhotosDto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ProfilePhotosDto getProfilePhotosDto() {
        return profilePhotosDto;
    }

    public void setProfilePhotosDto(ProfilePhotosDto profilePhotosDto) {
        this.profilePhotosDto = profilePhotosDto;
    }
}
