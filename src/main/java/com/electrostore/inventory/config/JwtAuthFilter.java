package com.electrostore.inventory.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Filtro JWT para validar y autenticar cada request usando el token JWT
// Este filtro se ejecuta una vez por cada request y verifica la cabecera Authorization
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    // Utilidad para operaciones con JWT (generar, validar, extraer datos)
    private final JwtUtil jwtUtil;
    // Servicio para obtener los datos del usuario desde el sistema
    private final UserDetailsService userDetailsService;

    // Constructor: inyecta las dependencias necesarias
    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // Metodo principal del filtro: intercepta cada request HTTP
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Obtiene la cabecera Authorization del request
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        // Verifica que la cabecera tenga el formato Bearer <token>
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Extrae el token JWT
            username = jwtUtil.getUsername(token); // Extrae el usuario del token
        }
        // Si hay usuario y no hay autenticacion previa en el contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Obtiene los datos del usuario desde el sistema
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // Valida el token JWT (firma y expiracion)
            if (jwtUtil.validateToken(token)) {
                // Crea el objeto de autenticacion para Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Establece la autenticacion en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Continua con el resto de la cadena de filtros
        chain.doFilter(request, response);
    }
}