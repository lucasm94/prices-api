package com.inditex.prices.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
@Profile("!local")
public class KafkaConfig {
    private final KafkaProperties kafkaProperties;
    @Bean
    public NewTopic priceUpdatesTopic() {
        return TopicBuilder.name(kafkaProperties.getTopicName())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
