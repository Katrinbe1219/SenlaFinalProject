package org.example.core.dto.getting.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistrictStatisticDto {
    private Long districtId;
    private String districtName;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;
    private BigDecimal avgPrice;
    private String categoryName;
    private Long categoryId;

}
