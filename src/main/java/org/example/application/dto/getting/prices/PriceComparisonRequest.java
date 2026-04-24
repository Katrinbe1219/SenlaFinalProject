package org.example.application.dto.getting.prices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceComparisonRequest {
    private Long goodId;
    private List<Long> shopIds;
}
