package Proconnect.backProconnect.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "WorkStations")
public class WorkStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "WorkStation is mandatory")
    @NotNull(message = "WorkStation cannot be null")
    private String workStation;
    
    public WorkStation() {
    }

    public WorkStation(Long id, Long userId, String workStation) {
        this.id = id;
        this.userId = userId;
        this.workStation = workStation;
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

    public String getWorkStation() {
        return workStation;
    }

    public void setWorkStation(String workStation) {
        this.workStation = workStation;
    }

}
