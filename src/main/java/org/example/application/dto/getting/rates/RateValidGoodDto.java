package org.example.application.dto.getting.rates;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.models.types.RatingTriggerType;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateValidGoodDto {
    private Long id;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant recalculatedAt;
    private Double rate;
    private RatingTriggerType triggeredBy;

}
