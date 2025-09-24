package com.electrostore.inventory.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.nio.charset.StandardCharsets;

/**
 * Utilidad para la generacion, validacion y extraccion de datos de tokens JWT.
 * Utiliza el algoritmo HS256 y una clave secreta segura.
 * Compatible con la version moderna de la libreria jjwt.
 */
@Component
public class JwtUtil {
    // Clave secreta para firmar los tokens (debe tener al menos 32 caracteres para HS256)
    // En produccion, se recomienda usar una variable de entorno para mayor seguridad.
    private static final String SECRET = "supersecretkeysupersecretkeysupersecretkey12";
    // Llave criptografica generada a partir de la clave secreta
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    // Tiempo de expiracion del token en milisegundos (1 hora)
    private final long EXPIRATION = 1000 * 60 * 60;

    /**
     * Genera un token JWT con el usuario y el rol especificados.
     * @param username Nombre de usuario (sub)
     * @param role Rol del usuario
     * @return Token JWT firmado
     */
    public String generateToken(String username, String role) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + EXPIRATION);
        // Se agregan los claims estandar y personalizados
        return Jwts.builder()
                .claim("sub", username) // Identificador del usuario
                .claim("role", role)    // Rol del usuario
                .claim("iat", issuedAt) // Fecha de emision
                .claim("exp", expiration) // Fecha de expiracion
                .signWith(key)           // Firma con la clave secreta
                .compact();
    }

    /**
     * Extrae el nombre de usuario (sub) del token JWT.
     * @param token Token JWT
     * @return Nombre de usuario
     */
    public String getUsername(String token) {
        // Se obtiene el claim "sub" del payload del token
        return (String) Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("sub");
    }

    /**
     * Extrae el rol del usuario del token JWT.
     * @param token Token JWT
     * @return Rol del usuario
     */
    public String getRole(String token) {
        // Se obtiene el claim "role" del payload del token
        return (String) Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role");
    }

    /**
     * Valida la firma y la estructura del token JWT.
     * @param token Token JWT
     * @return true si el token es valido, false si no
     */
    public boolean validateToken(String token) {
        try {
            // Intenta verificar la firma y parsear el token
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Si ocurre cualquier excepcion, el token no es valido
            return false;
        }
    }
}