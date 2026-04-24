package org.example.application.dto.creating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceCreateDto {
    private Long shopId;
    private Long goodId;
    private BigDecimal price;
}
