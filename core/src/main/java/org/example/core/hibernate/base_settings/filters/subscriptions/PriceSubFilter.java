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
import org.example.annotations.ValidDifference;
import org.example.core.hibernate.base_settings.sorting_types.PriceSubSortType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MutuallyExclusiveExtended(first = "curPrice", second = "minPrice", third = "maxPrice")
@MutuallyExclusiveExtended(first="curDate", second="startDate", third="endDate")
@ValidDateRange(first = "startDate", second = "endDate")
@ValidDifference(first = "minPrice", second = "maxPrice")
public class PriceSubFilter {
    @Size(min = 1, message = "userIds` length must be >0")
    private List<Long> userIds;
    @Size(min = 1, message = "goodIds` length must be >0")
    private List<Long> goodIds;
    @Size(min = 1, message = "shopIds` length must be >0")
    private List<Long> shopIds;

    @Positive(message = "minPrice must be >=0")
    private BigDecimal minPrice;
    @Positive(message = "maxPrice must be >=0")
    private BigDecimal maxPrice;
    @Positive(message = "curPrice must be >=0")
    private BigDecimal curPrice;

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
    private PriceSubSortType sortType = PriceSubSortType.ASC;

    @Builder.Default
    @PositiveOrZero(message = "page must be >=0")
    private  Integer page = 0;
    @Builder.Default
    @Positive(message = "page must be >0")
    private Integer size = null;




}
