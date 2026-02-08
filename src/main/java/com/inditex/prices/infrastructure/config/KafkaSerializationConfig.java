package com.inditex.prices.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inditex.prices.infrastructure.adapter.in.kafka.PriceUpdateMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!local")
public class KafkaSerializationConfig {
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(ObjectMapper objectMapper) {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "price-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<PriceUpdateMessage> deserializer = new JsonDeserializer<>(PriceUpdateMessage.class, objectMapper);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public KafkaTemplate<String, PriceUpdateMessage> kafkaTemplate(ObjectMapper objectMapper) {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        JsonSerializer<PriceUpdateMessage> serializer = new JsonSerializer<>(objectMapper);
        serializer.setAddTypeInfo(false);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props, new StringSerializer(), serializer));
    }
}
