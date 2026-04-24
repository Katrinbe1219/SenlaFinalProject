package org.example.application.hibernate.base_settings.filters.rates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.models.types.GoodStatusFromModerator;
import org.example.application.models.types.RatingStatus;
import org.example.application.models.types.RatingTriggerType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingRecalcFilter {
    //TODO проверка, что goodId и categoryId не могут быть одновременно
    private Long goodId;
    private Long categoryId;

    // TODO только для всех
    private List<Long> goodsIds;
    private List<Long> categoryIds;

    // TODO не может быть одновременно cur and min+max
    private Double curRate;
    private Double minRate;
    private Double maxRate;

    // TODO не может быть одновременно cur and min+max
    private LocalDate curDate;
    private LocalDate minDate;
    private LocalDate maxDate;

    private RatingStatus status;
    private RatingTriggerType triggerType;

}
