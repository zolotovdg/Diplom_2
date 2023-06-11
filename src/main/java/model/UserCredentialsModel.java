package model;

public class UserCredentialsModel {
    private String email;
    private String password;

    public UserCredentialsModel(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
