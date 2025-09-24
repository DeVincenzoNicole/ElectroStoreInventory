package com.electrostore.inventory.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryKafkaListener.class);

    @KafkaListener(topics = "inventory-events", groupId = "inventory-group")
    public void listen(String message) {
        log.info("[KAFKA EVENT] Recibido: {}", message);
    }
}
