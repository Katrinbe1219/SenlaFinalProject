package org.example.application.hibernate.base_settings.filters.rates;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.models.types.GoodStatusFromModerator;
import org.example.application.models.types.RatingStatus;
import org.example.application.models.types.RatingTriggerType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingRecalcFilter {
    //TODO проверка, что goodId и categoryId не могут быть одновременно
    @Positive(message = "goodId must be  > 0")
    private Long goodId;
    @Positive(message = "categoryId must be  > 0")
    private Long categoryId;

    // TODO только для всех
    private List<Long> goodsIds;
    private List<Long> categoryIds;

    // TODO не может быть одновременно cur and min+max
    @PositiveOrZero(message = "curRate must be >=0")
    private Double curRate;
    @PositiveOrZero(message = "minRate must be >=0")
    private Double minRate;
    @PositiveOrZero(message = "maxRate must be >=0")
    private Double maxRate;

    // TODO не может быть одновременно cur and min+max
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate curDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate minDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate maxDate;

    private RatingStatus status;
    private RatingTriggerType triggerType;

}
