package Proconnect.backProconnect.model;

import java.sql.Date;

public class PostulationDto {

    private Long Id;
    private Long UserId;
    private Long jobId;
    private Date date;

    public PostulationDto(Long id, Long userId, Long jobId, Date date) {
        Id = id;
        UserId = userId;
        this.jobId = jobId;
        this.date = date;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getUserId() {
        return UserId;
    }

    public void setUserId(Long userId) {
        UserId = userId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}