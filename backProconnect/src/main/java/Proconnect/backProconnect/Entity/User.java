package Proconnect.backProconnect.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "User")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is mandatory")
    @NotNull(message = "Name cannot be null")
    // Sin UNIQUE: dos personas pueden llamarse igual (el identificador público es userName).
    // Las BD existentes conservan el índice hasta la migración Flyway de restructure-models-performance.
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Username is mandatory")
    @NotNull(message = "Username cannot be null")
    @Column(unique = true,nullable = false)
    private String userName;

    @NotBlank(message = "Email is mandatory")
    @NotNull(message = "Email cannot be null")
    @Column(unique = true,nullable = false)
    private String email;

    @JsonIgnore
    @NotBlank(message = "Password is mandatory")
    @NotNull(message = "Password cannot be null")
    @Size(min = 7, max = 100, message = "Password be in range 7 at 100 characters long")
    @Column(nullable = false, length = 100)
    private String password;

    @NotBlank(message = "Role is mandatory")
    @NotNull(message = "Role cannot be null")
    private String role;

    
    public User() {
    }

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

    @JsonIgnore
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
