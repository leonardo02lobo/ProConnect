package Proconnect.backProconnect.model;

public class CompanyDto {
    
    private Long Id;
    private String name;
    private String description;
    private String location;
    private Long userId;
    public CompanyDto() {
    }
    public CompanyDto(Long id, String name, String description, String location, Long userId) {
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
