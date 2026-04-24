package org.example.application.hibernate.base_settings.filters.reviews;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.hibernate.base_settings.sorting_types.ReviewSortTypes;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAdvancedFilters {

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate lastDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate createdAt; //

    @Positive( message = "userId must be >0")
    private Long userId; //
    @Positive( message = "goodId must be >0")
    private Long goodId; //
    @PositiveOrZero(message = "rate must be >=0")
    private Integer rate; //
    private Boolean blocked; //
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate blockedAt; //
    private Long blockedBy; //


    @Builder.Default
    private ReviewSortTypes sortDir = ReviewSortTypes.ASC;

    @Builder.Default
    @PositiveOrZero(message = "page must be >=0")
    private  Integer page = null;
    @Builder.Default
    @Positive(message = "page must be >0")
    private Integer size = null;
}
