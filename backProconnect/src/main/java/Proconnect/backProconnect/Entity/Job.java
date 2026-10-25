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
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "Jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Company ID cannot be null") @Column(name = "company_id", nullable = false)
    private Long companyId;

    /** Relación de solo lectura (LAZY) sobre la FK company_id; se escribe con el campo Long. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_jobs_company"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Company company;

    @NotBlank(message = "Title is mandatory") @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is mandatory") @Column(nullable = false)
    private String description;

    @NotBlank(message = "WorkStation is mandatory") @Column(nullable = false)
    private String workStation;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Requerido por JPA para materializar la entidad. */
    public Job() {}

    public Job(
            Long id,
            Long companyId,
            String title,
            String description,
            String workStation,
            Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.workStation = workStation;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Company getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkStation() {
        return workStation;
    }

    public void setWorkStation(String workStation) {
        this.workStation = workStation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
