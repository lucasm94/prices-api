package com.inditex.prices;

import com.inditex.prices.infrastructure.adapter.in.kafka.PriceUpdateMessage;
import com.inditex.prices.infrastructure.adapter.out.persistence.PriceRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("integration")
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class PricesKafkaIntegrationTest {
    @Autowired
    private KafkaTemplate<String, PriceUpdateMessage> kafkaTemplate;

    @MockitoBean
    private PriceRepositoryAdapter priceAdapter;

    @Value("${kafka.topic-name}")
    private String topic;

    @Test
    void handlePriceUpdate_shouldInvalidatePrice_whenValidMessageReceived() throws InterruptedException {
        var date = LocalDateTime.now();
        var message = PriceUpdateMessage.builder()
                .productId(35455L)
                .brandId(1L)
                .date(date)
                .build();

        kafkaTemplate.send(topic, message);
        Thread.sleep(2000);

        verify(priceAdapter, times(1)).invalidatePrice(any(), eq(35455L), eq(1L));
    }
}
