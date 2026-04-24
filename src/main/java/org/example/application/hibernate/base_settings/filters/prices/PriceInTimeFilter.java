package org.example.application.hibernate.base_settings.filters.prices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PriceInTimeFilter {
    private Long shopId;
    private Long goodId;
    private LocalDate firstDate;
    private LocalDate lastDate;

}
