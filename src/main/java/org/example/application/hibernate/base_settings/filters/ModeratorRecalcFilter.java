package org.example.application.hibernate.base_settings.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.hibernate.base_settings.sorting_types.ModeratorRecalcSortType;
import org.example.application.models.types.ModeratorVerdict;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModeratorRecalcFilter {
    private List<Long> moderatorIds;
    private Long moderatorId;

    private List<Long> goodIds;
    private Long goodId;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate curDate;

    private ModeratorVerdict verdict;

    @Builder.Default
    private ModeratorRecalcSortType sortType = ModeratorRecalcSortType.ASC;

    @Builder.Default
    private Integer page = null;
    @Builder.Default
    private Integer count = null;
}
