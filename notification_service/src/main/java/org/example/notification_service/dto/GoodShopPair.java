package org.example.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public record GoodShopPair (
    Long goodId,
    Long shopId
){}
