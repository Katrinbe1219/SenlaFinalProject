package org.example.core.hibernate.base_settings.filters.rates;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusiveExtended;
import org.example.annotations.ValidDateRange;
import org.example.core.models.types.GoodStatusFromModerator;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidDateRange(first = "startDate", second = "endDate")
public class RatesFilter {
    @Positive(message = "goodId must be  > 0")
    @NotNull(message = "goodId must not be null")
    private Long goodId;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @NotNull(message = "startDate must not be null")
    private LocalDate startDate;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @NotNull(message = "endDate must not be null")
    private LocalDate endDate;

    private GoodStatusFromModerator goodStatus;
}
