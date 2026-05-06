package org.example.core.hibernate.base_settings.filters.prices;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class PriceInTimeFilter {
    @Positive(message = "shopId must be > 0")
    @NotNull(message = "shopId must not be null")
    private Long shopId;
    @Positive(message = "goodId must be > 0")
    @NotNull(message = "shopId must not be null")
    private Long goodId;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @NotNull(message = "startDate must be given")
    private LocalDate startDate;

    @NotNull(message = "endDate must be given")
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;

}
