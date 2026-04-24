package org.example.application.dto.getting.rates;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.models.types.RatingStatus;
import org.example.application.models.types.RatingTriggerType;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateFullDto {
    private Long id;
    private String goodName;
    private String categoryName;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant recalculatedAt;
    private RatingTriggerType triggeredBy;
    private RatingStatus ratingStatus;
    private String errorMessage;
    private Double rate;
}
