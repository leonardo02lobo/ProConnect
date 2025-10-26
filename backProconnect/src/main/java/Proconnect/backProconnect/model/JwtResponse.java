package Proconnect.backProconnect.model;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private UserDto userDto;

    public JwtResponse(String token, UserDto userDto) {
        this.token = token;
        this.userDto = userDto;
    }

    public JwtResponse() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserDto getUserDto() {
        return userDto;
    }

    public void setUserDto(UserDto userDto) {
        this.userDto = userDto;
    }

}
