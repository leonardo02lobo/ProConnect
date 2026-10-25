package Proconnect.backProconnect.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "Jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Company ID cannot be null") private Long companyId;

    @NotBlank(message = "Title is mandatory") @NotNull(message = "Title cannot be null") private String title;

    @NotBlank(message = "Description is mandatory") @NotNull(message = "Description cannot be null") private String description;

    @NotBlank(message = "WorkStation is mandatory") @NotNull(message = "WorkStation cannot be null") private String workStation;

    private Date date;

    /** Requerido por JPA para materializar la entidad. */
    public Job() {}

    public Job(
            Long id,
            Long companyId,
            String title,
            String description,
            String workStation,
            Date date) {
        this.id = id;
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.workStation = workStation;
        this.date = date;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
