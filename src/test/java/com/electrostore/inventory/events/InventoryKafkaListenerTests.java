package com.electrostore.inventory.events;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"inventory-events"})
@DirtiesContext
public class InventoryKafkaListenerTests {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static String receivedMessage;

    @KafkaListener(topics = "inventory-events", groupId = "test-group")
    public void listenTest(String message) {
        receivedMessage = message;
    }

    @Test
    void testKafkaSendAndReceive() throws InterruptedException {
        String testMsg = "Test Kafka Message";
        receivedMessage = null;
        kafkaTemplate.send("inventory-events", testMsg);
        Thread.sleep(1000); // Espera breve para recibir el mensaje
        assertThat(receivedMessage).isEqualTo(testMsg);
    }
}
