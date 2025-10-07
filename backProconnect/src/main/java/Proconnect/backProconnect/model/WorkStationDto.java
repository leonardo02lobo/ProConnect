package Proconnect.backProconnect.model;

public class WorkStationDto {
    
    private Long id;
    private Long userId;
    private String workStation;

    public WorkStationDto() {
    }

    public WorkStationDto(Long id, Long userId, String workStation) {
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
