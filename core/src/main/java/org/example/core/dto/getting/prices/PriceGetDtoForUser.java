package org.example.core.dto.getting.prices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceGetDtoForUser {
    private Long shopId;
    private String shopName;
    private String address;

    private String goodName;
    private BigDecimal price;
}
