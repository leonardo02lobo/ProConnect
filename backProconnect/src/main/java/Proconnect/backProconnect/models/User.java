package Proconnect.backProconnect.models;

import org.hibernate.validator.constraints.UniqueElements;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is mandatory")
    @NotNull(message = "Name cannot be null")
    @UniqueElements
    private String name;

    @NotBlank(message = "Username is mandatory")
    @NotNull(message = "Username cannot be null")
    @UniqueElements
    private String userName;

    @NotBlank(message = "Email is mandatory")
    @NotNull(message = "Email cannot be null")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @NotNull(message = "Password cannot be null")
    @Min(value = 7, message = "Password must be at least 7 characters long")
    @Max(value = 20, message = "Password must be at most 14 characters long")
    private String password;

    @NotBlank(message = "Role is mandatory")
    @NotNull(message = "Role cannot be null")
    private String role;

    public User(Long id, String name, String userName, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.role = role;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
