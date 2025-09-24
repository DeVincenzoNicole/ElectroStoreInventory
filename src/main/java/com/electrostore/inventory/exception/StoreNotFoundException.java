package com.electrostore.inventory.exception;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(Long storeId) {
        super("La sucursal " + storeId + " no existe.");
    }
}
