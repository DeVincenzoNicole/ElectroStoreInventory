package com.electrostore.inventory.service;

import org.springframework.context.ApplicationEvent;

public class InventoryChangeEvent extends ApplicationEvent {
    private final Long productId;
    private final Long storeId;
    private final String action;
    private final int quantity;

    public InventoryChangeEvent(Object source, Long productId, Long storeId, String action, int quantity) {
        super(source);
        this.productId = productId;
        this.storeId = storeId;
        this.action = action;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public Long getStoreId() { return storeId; }
    public String getAction() { return action; }
    public int getQuantity() { return quantity; }
}
