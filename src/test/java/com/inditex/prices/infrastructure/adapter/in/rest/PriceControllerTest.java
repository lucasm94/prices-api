package com.inditex.prices.infrastructure.adapter.in.rest;

import com.inditex.prices.application.usecase.PriceUseCase;
import com.inditex.prices.domain.exception.NotFoundException;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.monitoring.MetricsRecorder;
import com.inditex.prices.infrastructure.monitoring.MetricsType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PriceController.class)
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceUseCase priceUseCase;

    @MockitoBean
    private MetricsRecorder metrics;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss");

    @Test
    void shouldReturn200AndCorrectJson_WhenRequestIsValid() throws Exception {
        // Given
        var dateStr = "2020-06-14-10.00.00";
        var date = LocalDateTime.parse(dateStr, FORMATTER);
        var price = Price.builder()
                .brandId(1L)
                .startDate(date.minusHours(1))
                .endDate(date.plusHours(1))
                .priceList(1)
                .productId(35455L)
                .priority(1)
                .price(new BigDecimal("35.50"))
                .currency("EUR")
                .build();

        when(priceUseCase.getPrice(date, 35455L, 1L)).thenReturn(price);

        // When
        var mvcResult = mockMvc.perform(get("/v1/prices")
                        .param("date", dateStr)
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.startDate").value(date.minusHours(1).format(FORMATTER)))
                .andReturn();

        // Then
        verify(priceUseCase, times(1)).getPrice(date, 35455L, 1L);
        verifyNoInteractions(metrics);
        var content = mvcResult.getResponse().getContentAsString();
        assertThat(content).isNotBlank();
    }

    @Test
    void shouldReturn400AndErrorResponse_WhenDateHasInvalidFormat() throws Exception {
        // Given
        var badDate = "invalid-date";

        // When
        mockMvc.perform(get("/v1/prices")
                        .param("date", badDate)
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Parameter 'date' has an invalid value")));

        // Then
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.BAD_REQUEST));
    }

    @Test
    void shouldReturn400AndErrorResponse_WhenMissingParameter() throws Exception {
        // Given: omit brandId

        // When
        mockMvc.perform(get("/v1/prices")
                        .param("date", "2020-06-14-10.00.00")
                        .param("productId", "35455")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Required parameter is missing: brandId")));

        // Then
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.BAD_REQUEST));
    }

    @Test
    void shouldReturn404AndErrorResponse_WhenPriceNotFound() throws Exception {
        // Given
        var dateStr = "2020-06-14-10.00.00";
        var date = LocalDateTime.parse(dateStr, FORMATTER);
        when(priceUseCase.getPrice(date, 35455L, 1L)).thenThrow(new NotFoundException("not found"));

        // When
        mockMvc.perform(get("/v1/prices")
                        .param("date", dateStr)
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Price not found")));

        // Then
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.NOT_FOUND));
    }

    @Test
    void shouldReturn400AndErrorResponse_WhenProductIdIsNegative() throws Exception {
        // Given

        // When
        mockMvc.perform(get("/v1/prices")
                        .param("date", "2020-06-14-10.00.00")
                        .param("productId", "-1")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Product ID must be positive")));

        // Then
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.BAD_REQUEST));
    }

}
