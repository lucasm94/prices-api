package com.inditex.prices.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

@Configuration
@Profile("local")
public class LocalKafkaConfig {
    @Bean
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        EmbeddedKafkaZKBroker broker = new EmbeddedKafkaZKBroker(1, true);
        broker.kafkaPorts(9092);
        broker.brokerProperty("listeners", "PLAINTEXT://localhost:9092");
        broker.brokerProperty("auto.create.topics.enable", "true");

        return broker;
    }
}
