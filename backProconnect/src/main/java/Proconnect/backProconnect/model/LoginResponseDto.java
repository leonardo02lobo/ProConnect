package Proconnect.backProconnect.model;

public class LoginResponseDto {

    private boolean success;
    private String message;
    private UserDto data;

    public LoginResponseDto() {
    }

    public LoginResponseDto(boolean success, String message, UserDto data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserDto getData() {
        return data;
    }

    public void setData(UserDto data) {
        this.data = data;
    }

}
