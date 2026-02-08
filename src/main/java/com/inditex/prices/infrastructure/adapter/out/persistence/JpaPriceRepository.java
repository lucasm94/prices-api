package com.inditex.prices.infrastructure.adapter.out.persistence;

import com.inditex.prices.infrastructure.adapter.out.persistence.entity.PriceEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Spring Data JPA repository for PriceEntity.
 */
public interface JpaPriceRepository extends JpaRepository<PriceEntity, Long> {
    @Query("""
            SELECT p FROM PriceEntity p WHERE p.productId = :productId AND p.brandId = :brandId
            AND :date BETWEEN p.startDate AND p.endDate ORDER BY p.priority DESC LIMIT 1
            """)
    Optional<PriceEntity> findTopPrice(@Param("date") LocalDateTime date, @Param("productId") Long productId,
                                       @Param("brandId") Long brandId);
}
