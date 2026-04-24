package org.example.core.dto.getting.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartStatisticRequest {
    private List<Long> goodIds;
    private List<Long> shopIds;
}
