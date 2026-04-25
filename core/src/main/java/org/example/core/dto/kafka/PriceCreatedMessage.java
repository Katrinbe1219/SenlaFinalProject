package org.example.core.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceCreatedMessage {
    private Long goodId;
    private Long shopId;
    private BigDecimal price;
}
