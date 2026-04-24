package org.example.application.dto.getting.statistics.shops;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopStatisticDto {
    private Long id;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;
    private BigDecimal avgPrice;
}
