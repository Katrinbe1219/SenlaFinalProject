package org.example.application.dto.getting.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecalculationForGoodDto {
    private Long goodId;
    private Double rate;
}
