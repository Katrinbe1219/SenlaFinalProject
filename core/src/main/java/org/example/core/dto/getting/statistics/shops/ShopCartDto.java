package org.example.core.dto.getting.statistics.shops;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopCartDto {
    private Long shopId;
    private String shopName;

    private BigDecimal totalPrice;
    private String outOfStockGoods;

}
