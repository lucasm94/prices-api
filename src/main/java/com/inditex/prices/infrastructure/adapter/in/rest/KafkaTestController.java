package com.inditex.prices.infrastructure.adapter.in.rest;

import com.inditex.prices.infrastructure.adapter.in.kafka.PriceUpdateMessage;
import com.inditex.prices.infrastructure.config.KafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to simulate external system events.
 * Provides an endpoint to manually trigger Kafka messages for validation purposes.
 */
@RestController
@RequestMapping("/v1/internal")
@RequiredArgsConstructor
public class KafkaTestController {
    private final KafkaTemplate<String, PriceUpdateMessage> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    /**
     * Publishes a price update event to the configured Kafka topic.
     * @param message The price update details to be sent as a message.
     * @return A response entity confirming the message dispatch.
     */
    @PostMapping("/publish")
    public ResponseEntity<String> publish(@RequestBody PriceUpdateMessage message) {
        kafkaTemplate.send(kafkaProperties.getTopicName(), message);
        return ResponseEntity.ok("Message sent to kafka.");
    }
}
