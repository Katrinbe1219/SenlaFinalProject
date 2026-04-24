package org.example.core.dto.getting.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodForReviewDto {
    private Long id;
    private String name;
    private Double averageRate;
}
