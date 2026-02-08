package com.inditex.prices.infrastructure.adapter.out.persistence.entity;

import com.inditex.prices.domain.model.Price;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRICES")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PriceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "BRAND_ID")
    private Long brandId;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;

    @Column(name = "PRICE_LIST")
    private Integer priceList;

    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "PRIORITY")
    private Integer priority;

    @Column(name = "PRICE")
    private BigDecimal price;

    @Column(name = "CURR")
    private String currency;

    /**
     * Maps the database entity to our price domain.
     */
    public Price toDomain() {
        return Price.builder()
                .brandId(this.brandId)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .priceList(this.priceList)
                .productId(this.productId)
                .priority(this.priority)
                .price(this.price)
                .currency(this.currency)
                .build();
    }
}
