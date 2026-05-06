package org.example.core.hibernate.base_settings.filters.rates;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusive;
import org.example.annotations.MutuallyExclusiveExtended;
import org.example.annotations.ValidDateRange;
import org.example.annotations.ValidDifference;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.RatingStatus;
import org.example.core.models.types.RatingTriggerType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@MutuallyExclusive(fields1 = "goodId", fields2 = "goodIds")
@MutuallyExclusive(fields1 = "categoryId", fields2 = "categoryIds")
@MutuallyExclusive(fields1 = "tagId", fields2 = "tagIds")
@MutuallyExclusiveExtended(first = "curRate", second="maxRate", third = "minRate")
@MutuallyExclusiveExtended(first = "curDate", second="startDate", third = "endDate")
@MutuallyExclusiveExtended(first="goodId", second="categoryId", third="categoryIds")
@ValidDateRange(first = "startDate", second = "endDate")
@ValidDifference(first = "minRate", second = "maxRate")
public class RatingRecalcFilter {

    @Positive(message = "goodId must be  > 0")
    private Long goodId;
    @Positive(message = "categoryId must be  > 0")
    private Long categoryId;
    @Positive(message = "tagId must be > 0")
    private Long tagId;


    @Size(min=1, message = "goodIds length must be > 0")
    private List<Long> goodIds;
    @Size(min=1, message = "categoryIds length must be > 0")
    private List<Long> categoryIds;
    @Size(min=1, message = "tagIds length must be > 0")
    private List<Long> tagIds;


    @PositiveOrZero(message = "curRate must be >=0")
    private Double curRate;
    @PositiveOrZero(message = "minRate must be >=0")
    private Double minRate;
    @PositiveOrZero(message = "maxRate must be >=0")
    private Double maxRate;


    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate curDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;

    private RatingStatus status;
    private RatingTriggerType triggerType;

}
