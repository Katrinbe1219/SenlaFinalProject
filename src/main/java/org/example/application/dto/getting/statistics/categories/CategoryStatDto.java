package org.example.application.dto.getting.statistics.categories;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal avgPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long countGoods;
}
