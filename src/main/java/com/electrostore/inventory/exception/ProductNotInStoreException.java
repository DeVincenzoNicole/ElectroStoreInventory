package com.electrostore.inventory.exception;

public class ProductNotInStoreException extends RuntimeException {
    public ProductNotInStoreException(Long productId, Long storeId) {
        super("El producto " + productId + " no existe en la sucursal " + storeId);
    }
}