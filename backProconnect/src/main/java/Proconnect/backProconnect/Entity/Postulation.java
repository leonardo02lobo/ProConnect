package Proconnect.backProconnect.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

/** Postulación a un empleo. UNIQUE(user_id, job_id): una por usuario y empleo. */
@Entity
@Table(
        name = "Postulations",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_postulations_user_job",
                        columnNames = {"user_id", "job_id"}))
public class Postulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID cannot be null") @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Relación de solo lectura (LAZY) sobre la FK user_id; se escribe con el campo Long. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_postulations_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @NotNull(message = "Job ID cannot be null") @Column(name = "job_id", nullable = false)
    private Long jobId;

    /** Relación de solo lectura (LAZY) sobre la FK job_id; se escribe con el campo Long. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "job_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_postulations_job"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Job job;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 16)
    private PostulationStatus status = PostulationStatus.PENDING;

    /** Requerido por JPA para materializar la entidad. */
    public Postulation() {}

    public Postulation(Long id, Long userId, Long jobId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.jobId = jobId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
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

    public User getUser() {
        return user;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Job getJob() {
        return job;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public PostulationStatus getStatus() {
        return status;
    }

    public void setStatus(PostulationStatus status) {
        this.status = status;
    }
}
