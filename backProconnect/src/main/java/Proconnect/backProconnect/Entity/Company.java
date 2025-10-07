package Proconnect.backProconnect.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Company")
public class Company {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotBlank(message = "Name is mandatory")
    @NotNull(message = "Name cannot be null")
    private String name;

    @NotBlank(message = "Description is mandatory")
    @NotNull(message = "Description cannot be null")
    private String description;

    @NotBlank(message = "Location is mandatory")
    @NotNull(message = "Location cannot be null")
    private String location;
    
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    public Company(Long id, String name, String description, String location, Long userId) {
        Id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.userId = userId;   
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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
    
}
