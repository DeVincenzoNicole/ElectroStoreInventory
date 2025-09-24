package com.electrostore.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryEventListener.class);

    @EventListener
    public void handleInventoryChange(InventoryChangeEvent event) {
        log.info("[EVENT] Accion: {} | Producto: {} | Sucursal: {} | Cantidad: {}", event.getAction(), event.getProductId(), event.getStoreId(), event.getQuantity());
    }
}
