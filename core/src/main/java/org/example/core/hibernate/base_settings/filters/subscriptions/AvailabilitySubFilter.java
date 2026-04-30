package org.example.core.hibernate.base_settings.filters.subscriptions;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusiveExtended;
import org.example.core.hibernate.base_settings.sorting_types.AvailabilitySubSortType;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MutuallyExclusiveExtended(first="curDate", second="startDate", third = "endDate")
public class AvailabilitySubFilter {
    @Size(min=1, message = "userIds length must be >0")
    private List<Long> userIds;

    @Size(min=1, message = "goodIds length must be >0")
    List<Long> goodIds;

    @Size(min=1, message = "shopIds length must be > 0")
    List<Long> shopIds;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate curDate;

    @Builder.Default
    @NotNull(message = "if you are undecided about the sortType, do not provide it")
    private AvailabilitySubSortType sortType = AvailabilitySubSortType.ASC;

    @Builder.Default
    @PositiveOrZero(message = "page must be >=0")
    private  Integer page = null;
    @Builder.Default
    @Positive(message = "page must be >0")
    private Integer size = null;
}
