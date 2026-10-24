package Proconnect.backProconnect.Entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Postulations")
public class Postulation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    
    @NotNull(message = "Job ID cannot be null")
    private Long jobId;

    private Date date;

    /**
     * Columna creada por ddl-auto=update con DEFAULT 'PENDING' para rellenar las filas existentes; la migración
     * Flyway de restructure-models-performance la captura en su baseline.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(16) not null default 'PENDING'")
    private PostulationStatus status = PostulationStatus.PENDING;

    /** Requerido por JPA para materializar la entidad. */
    public Postulation() {
    }

    public Postulation(Long id, Long userId, Long jobId, Date date) {
        this.id = id;
        this.userId = userId;
        this.jobId = jobId;
        this.date = date;
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

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public PostulationStatus getStatus() {
        return status;
    }

    public void setStatus(PostulationStatus status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    
}
