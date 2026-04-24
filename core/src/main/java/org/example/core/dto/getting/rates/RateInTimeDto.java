package org.example.core.dto.getting.rates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateInTimeDto {
    private Instant createdAt;
    private Double rate;
}
