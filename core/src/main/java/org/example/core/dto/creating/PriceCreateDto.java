package org.example.core.dto.creating;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceCreateDto {
    @NotNull(message = "shopId must be >0")
    private Long shopId;
    @NotNull(message = "goodId must be >0")
    private Long goodId;
    @NotNull(message = "price must be >0")
    private BigDecimal price;
}
