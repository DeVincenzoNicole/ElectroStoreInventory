package com.electrostore.inventory.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("El producto " + productId + " no existe.");
    }
}
