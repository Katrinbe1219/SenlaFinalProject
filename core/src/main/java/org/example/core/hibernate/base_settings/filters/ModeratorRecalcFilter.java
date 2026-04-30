package org.example.core.hibernate.base_settings.filters;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusive;
import org.example.annotations.MutuallyExclusiveExtended;
import org.example.core.hibernate.base_settings.sorting_types.ModeratorRecalcSortType;
import org.example.core.models.types.ModeratorVerdict;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MutuallyExclusive(fields1 = "moderatorId", fields2 ="moderatorIds" )
@MutuallyExclusive(fields1 = "goodIds", fields2 = "goodId")
@MutuallyExclusiveExtended(first="curDate", second = "endDate", third = "startDate")
public class ModeratorRecalcFilter {
    private List<Long> moderatorIds;
    @Positive( message = "moderatorId must be > 0")
    private Long moderatorId;

    private List<Long> goodIds;
    @Positive( message = "goodId must be > 0")
    private Long goodId;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate curDate;

    private ModeratorVerdict verdict;

    @Builder.Default
    @NotNull(message = "if you are undecided about the sortType, do not provide it")
    private ModeratorRecalcSortType sortType = ModeratorRecalcSortType.ASC;

    @Builder.Default
    @PositiveOrZero(message = "page must be >=0")
    private Integer page = null;

    @Builder.Default
    @Positive( message = "count > 0")
    private Integer count = null;
}
