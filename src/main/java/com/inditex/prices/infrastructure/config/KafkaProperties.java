package com.inditex.prices.infrastructure.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    private String topicName;
}
