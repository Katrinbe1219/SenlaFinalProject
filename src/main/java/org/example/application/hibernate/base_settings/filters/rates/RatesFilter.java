package org.example.application.hibernate.base_settings.filters.rates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.models.types.GoodStatusFromModerator;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatesFilter {
    private Long goodId;
    private LocalDate firstDate;
    private LocalDate lastDate;
    private GoodStatusFromModerator goodStatus;
}
