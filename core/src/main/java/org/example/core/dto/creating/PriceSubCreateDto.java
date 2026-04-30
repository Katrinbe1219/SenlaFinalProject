package org.example.core.dto.creating;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceSubCreateDto {
    @Positive(message = "userId must be >0")
    private Long userId;

    @Positive(message = "goodId must be > 0")
    private Long goodId;

    @NotNull(message = "shopId must be > 0")
    private Long shopId;

    @NotNull(message = "price must be > 0")
    private BigDecimal price;
}
