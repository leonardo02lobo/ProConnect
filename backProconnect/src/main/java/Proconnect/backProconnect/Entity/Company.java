package Proconnect.backProconnect.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Empresa. ON DELETE RESTRICT sobre el propietario: no se borra un usuario que es dueño de una
 * empresa (sus empleos y postulaciones son información de terceros).
 */
@Entity
@Table(name = "Company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is mandatory") @Column(nullable = false)
    private String name;

    @NotBlank(message = "Description is mandatory") @Column(nullable = false)
    private String description;

    @NotBlank(message = "Location is mandatory") @Column(nullable = false)
    private String location;

    @NotNull(message = "User ID cannot be null") @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Relación de solo lectura (LAZY) sobre la FK user_id; se escribe con el campo Long. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_company_user"))
    private User owner;

    /** Requerido por JPA para materializar la entidad. */
    public Company() {}

    public Company(Long id, String name, String description, String location, Long userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.userId = userId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getOwner() {
        return owner;
    }
}
