package org.example.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceChangedMessage {
    private Long goodId;
    private Long shopId;
    private BigDecimal newPrice;
}
