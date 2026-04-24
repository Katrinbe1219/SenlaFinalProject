package org.example.application.hibernate.base_settings.filters.reviews;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.hibernate.base_settings.sorting_types.ReviewSortTypes;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAdvancedFilters {

    private LocalDate startDate;
    private LocalDate lastDate;
    private LocalDate createdAt; //


    private Long userId; //
    private Long goodId; //
    private Integer rate; //
    private Boolean blocked; //
    private LocalDate blockedAt; //
    private Long blockedBy; //


    @Builder.Default
    private ReviewSortTypes sortDir = ReviewSortTypes.ASC;

    @Builder.Default
    private  Integer page = 0;
    @Builder.Default
    private Integer size = 20;
}
