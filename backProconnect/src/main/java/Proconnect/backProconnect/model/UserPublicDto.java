package Proconnect.backProconnect.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Representación pública de un usuario: nunca incluye el password. {@code avatarUrl}/{@code
 * coverUrl} son nombres de archivo del servicio de media (columnas de {@code user} desde V3). El
 * email solo se rellena para el propio usuario o un ADMIN; en vistas de terceros es null y no se
 * serializa.
 */
public class UserPublicDto {

    private Long id;
    private String name;
    private String userName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;

    private String role;
    private String avatarUrl;
    private String coverUrl;

    public UserPublicDto() {}

    public UserPublicDto(
            Long id,
            String name,
            String userName,
            String email,
            String role,
            String avatarUrl,
            String coverUrl) {
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.email = email;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.coverUrl = coverUrl;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
