package org.example.core.hibernate.base_settings.service_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckForPriceSubscription {
    private Long shopId;
    private Long goodId;
    private Long priceId;
    private BigDecimal price;
    private Long priceSubId; // checking existence of sub

    public CheckForPriceSubscription(Long goodId, Long priceSubId){
        this.goodId = goodId;
        this.priceSubId = priceSubId;
    }
}
