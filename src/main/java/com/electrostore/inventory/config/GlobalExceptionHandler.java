package com.electrostore.inventory.config;

import com.electrostore.inventory.exception.ProductNotFoundException;
import com.electrostore.inventory.exception.ProductNotInStoreException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Maneja errores de argumentos invalidos en los endpoints REST.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body("Argumento invalido: " + ex.getMessage());
    }

    /**
     * Maneja cualquier excepcion no controlada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + ex.getMessage());
    }

    /**
     * Maneja el caso donde el producto no existe.
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Maneja el caso donde el producto no esta en la sucursal consultada.
     */
    @ExceptionHandler(ProductNotInStoreException.class)
    public ResponseEntity<String> handleProductNotInStore(ProductNotInStoreException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Maneja el caso donde el usuario esta logueado pero no tiene permisos suficientes.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no habilitado para realizar esta accion.");
    }
}