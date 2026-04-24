package org.example.core.dto.getting.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodAnalyseForShopDto {
    private Long goodId;
    private String goodName;
    private BigDecimal price;
    private Long priceId;
}
