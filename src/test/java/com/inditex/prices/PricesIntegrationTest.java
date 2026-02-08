package com.inditex.prices;

import com.inditex.prices.infrastructure.adapter.in.kafka.PriceUpdateMessage;
import com.inditex.prices.infrastructure.adapter.in.rest.dto.PriceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Sql(scripts = "/data.sql", config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
@ImportAutoConfiguration(exclude = org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class)
class PricesIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private KafkaTemplate<String, PriceUpdateMessage> kafkaTemplate;

	@Test
	void contextLoads() {
	}

    @ParameterizedTest
    @CsvSource({
            "2020-06-14-10.00.00, 35455, 1, 35.50, 1",
            "2020-06-14-16.00.00, 35455, 1, 25.45, 2",
            "2020-06-14-21.00.00, 35455, 1, 35.50, 1",
            "2020-06-15-10.00.00, 35455, 1, 30.50, 3",
            "2020-06-16-21.00.00, 35455, 1, 38.95, 4"
    })
    void getPrice_shouldReturnExpectedRate(String date, Long productId, Long brandId, BigDecimal expectedPrice,
                                           int expectedPriceList) {
        String url = String.format("/v1/prices?date=%s&productId=%d&brandId=%d", date, productId, brandId);

        var response = restTemplate.getForEntity(url, PriceResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        PriceResponse body = response.getBody();
        assertNotNull(body);

        assertEquals(0, expectedPrice.compareTo(body.price()), "Price mismatch for date: " + date);
        assertEquals(expectedPriceList, body.priceList(), "Price list mismatch");
    }

    @Test
    void getPrice_shouldReturn404_whenProductDoesNotExist() {
        Long unknownProductId = 99999L;
        String url = String.format("/v1/prices?date=2020-06-14-10.00.00&productId=%d&brandId=1", unknownProductId);

        var response = restTemplate.getForEntity(url, Object.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getPrice_shouldReturn400_whenDateIsInvalid() {
        String invalidDate = "2020/06/14";
        String url = String.format("/v1/prices?date=%s&productId=35455&brandId=1", invalidDate);

        var response = restTemplate.getForEntity(url, Object.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
