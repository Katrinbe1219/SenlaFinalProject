package org.example.core.dto.getting.prices;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceComparisonRequest {
    @NotNull(message = "goodId can not be null")
    private Long goodId;
    @Size(min = 1, message = "shopIds` length must be > 0")
    private List<Long> shopIds;
}
