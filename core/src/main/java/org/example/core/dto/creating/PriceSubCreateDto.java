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
    @NotNull(message = "goodId can not be null")
    private Long goodId;

    @NotNull(message = "shopId must be given")
    @Positive(message = "shopId must be > 0")
    private Long shopId;

    @NotNull(message = "price must be given")
    @Positive(message = "price must be > 0")
    private BigDecimal price;
}
