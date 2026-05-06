package org.example.core.hibernate.base_settings.filters.subscriptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusiveExtended;
import org.example.annotations.ValidDateRange;
import org.example.core.hibernate.base_settings.sorting_types.AvailabilitySubSortType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MutuallyExclusiveExtended(first="curDate", second="startDate", third = "endDate")
@ValidDateRange(first = "startDate", second = "endDate")
public class AvailabilitySubFilter {

    @Size(min=1, message = "userIds length must be >0")
    private List<Long> userIds;

    @Size(min=1, message = "goodIds length must be >0")
    List<Long> goodIds;

    @Size(min=1, message = "shopIds length must be > 0")
    List<Long> shopIds;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate curDate;

    @Builder.Default
    @NotNull(message = "if you are undecided about the sortType, do not provide it")
    private AvailabilitySubSortType sortType = AvailabilitySubSortType.ASC;

    @Builder.Default
    @PositiveOrZero(message = "page must be >=0")
    private  Integer page = 0;
    @Builder.Default
    @Positive(message = "page must be >0")
    private Integer size = null;
}
