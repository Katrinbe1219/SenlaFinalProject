package org.example.core.dto.getting.subscriptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.getting.statistics.shops.ShopGetDto;
import org.example.core.dto.getting.users.ModeratorSmallDto;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceSubGetDto {
    @Positive(message = "id must be > 0")
    private Long id;
    private GoodIdDto good;
    private ShopGetDto shop;
    @Positive(message = "targetPrice must be > 0")
    private BigDecimal targetPrice;

    private ModeratorSmallDto user;
    @JsonFormat(pattern = "dd.MM.yyyy", timezone = "Europe/Moscow")
    private Instant createdAt;
}
