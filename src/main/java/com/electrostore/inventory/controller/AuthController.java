package com.electrostore.inventory.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.electrostore.inventory.config.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Operation(
        summary = "Autenticacion de usuario y obtencion de token JWT",
        description = "Envía las credenciales de usuario para obtener un token JWT.\n\nEjemplo de credenciales:\n- admin / adminpass (rol ADMIN)\n- user / userpass (rol USER)\n\nEl token debe usarse en el boton 'Authorize' de Swagger anteponiendo 'Bearer '.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(
                    example = "{\n  \"username\": \"admin\",\n  \"password\": \"adminpass\"\n}"
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Token JWT generado correctamente",
                content = @Content(
                    schema = @Schema(
                        example = "{ \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\" }"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Credenciales inválidas"
            )
        }
    )
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
    	log.info("Body recibido: " + body);
        String username = body.get("username");
        String password = body.get("password");
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails != null && userDetails.getPassword() != null &&
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(password, userDetails.getPassword())) {
            String token = jwtUtil.generateToken(username, userDetails.getAuthorities().iterator().next().getAuthority());
            return Map.of("token", token);
        } else {
            throw new RuntimeException("Credenciales inválidas");
        }
    }
}