package Proconnect.backProconnect.Entity;

import Proconnect.backProconnect.model.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "User",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
            @UniqueConstraint(name = "uk_user_user_name", columnNames = "user_name")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sin UNIQUE: dos personas pueden llamarse igual (el identificador público es userName).
    @NotBlank(message = "Name is mandatory") @Column(nullable = false)
    private String name;

    @NotBlank(message = "Username is mandatory") @Column(nullable = false)
    private String userName;

    @NotBlank(message = "Email is mandatory") @Column(nullable = false)
    private String email;

    @JsonIgnore
    @NotBlank(message = "Password is mandatory") @Size(min = 7, max = 100, message = "Password be in range 7 at 100 characters long") @Column(nullable = false, length = 100)
    private String password;

    /** USER, COMPANY_ADMIN o ADMIN, persistido como texto (CHECK ck_user_role). */
    @NotNull(message = "Role cannot be null") @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 16)
    private Role role = Role.USER;

    public User() {}

    public User(Long id, String name, String userName, String email, String password, Role role) {
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
