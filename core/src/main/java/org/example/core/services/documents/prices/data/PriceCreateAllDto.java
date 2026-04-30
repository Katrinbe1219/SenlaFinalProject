package org.example.core.services.documents.prices.data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.models.Good;
import org.example.core.models.Shop;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceCreateAllDto {
    @NotNull
    private Good good;
    @NotNull
    private Shop shop;
    @NotNull
    @Positive
    private BigDecimal price;
}
