package Proconnect.backProconnect.model;

import java.util.Date;

public class JobDto {

    private Long Id;
    private Long companyId;
    private String title;
    private String description;
    private String workStation;
    private Date date;

    public JobDto() {
    }

    public JobDto(Long id, Long companyId, String title, String description, String workStation, Date date) {
        Id = id;
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.workStation = workStation;
        this.date = date;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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
