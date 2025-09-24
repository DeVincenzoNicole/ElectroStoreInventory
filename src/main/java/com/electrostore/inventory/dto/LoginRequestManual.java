package com.electrostore.inventory.dto;

/**
 * DTO manual para recibir las credenciales de login (sin Lombok ni anotaciones).
 */
public class LoginRequestManual {
    private String username;
    private String password;

    public LoginRequestManual() {}

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}