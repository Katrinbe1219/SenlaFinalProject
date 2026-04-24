package org.example.application.dto.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceHistoryByGoodAndShop {
    private Long priceId;
    private BigDecimal price;
    private Instant validTo;
    private Instant validFrom;
}
