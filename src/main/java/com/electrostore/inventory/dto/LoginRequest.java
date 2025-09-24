package com.electrostore.inventory.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir las credenciales de login.
 */
@Data
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}