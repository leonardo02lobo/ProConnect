package Proconnect.backProconnect.model;

public class FriendDto {

    private Long id;
    private Long userIdFollow;
    private Long userIdBeFollowed;
    private String status;

    public FriendDto() {
    }

    public FriendDto(Long userIdFollow, Long userIdBeFollowed, String status) {
        this.userIdFollow = userIdFollow;
        this.userIdBeFollowed = userIdBeFollowed;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserIdFollow() {
        return userIdFollow;
    }

    public void setUserIdFollow(Long userIdFollow) {
        this.userIdFollow = userIdFollow;
    }

    public Long getUserIdBeFollowed() {
        return userIdBeFollowed;
    }

    public void setUserIdBeFollowed(Long userIdBeFollowed) {
        this.userIdBeFollowed = userIdBeFollowed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
