package org.example.core.dto.getting.prices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceInTime {
    private Double price;
    private Instant validFrom;
    private Instant validTo;

}
