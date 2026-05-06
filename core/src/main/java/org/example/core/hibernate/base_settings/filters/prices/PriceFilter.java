package org.example.core.hibernate.base_settings.filters.prices;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusive;
import org.example.annotations.MutuallyExclusiveExtended;
import org.example.annotations.ValidDateRange;
import org.example.annotations.ValidDifference;
import org.example.core.hibernate.base_settings.sorting_types.PriceSortTypes;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@MutuallyExclusiveExtended(first = "curPrice", second = "maxPrice", third = "minPrice")
@MutuallyExclusive(fields1 = "current", fields2 = "old")
@MutuallyExclusive(fields2 = "shopIds", fields1 = "districtIds")
@ValidDateRange(first = "startDate", second = "endDate")
@ValidDifference(first = "minPrice", second = "maxPrice")
public class PriceFilter {
    private List<Long> categoryIds;
    private List<Long> shopIds;

    //  взаимоисключающие
    private List<Long> goodIds;
    private List<Long> districtIds;

    //  2  против 1
    @PositiveOrZero(message = "minPrice must be  >= 0")
    private BigDecimal  minPrice;
    @PositiveOrZero(message = "maxPrice must be  >= 0")
    private BigDecimal  maxPrice;
    @PositiveOrZero(message = "curPrice must be  >= 0")
    private BigDecimal curPrice;

    //  mutually exclusive
    private Boolean current;
    private Boolean old;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;

    @Builder.Default
    @NotNull(message = "if you are undecided about the sortType, do not provide it")
    private PriceSortTypes sortType = PriceSortTypes.ASC;

    @Builder.Default
    @PositiveOrZero(message = "page must be >=0")
    private  Integer page = 0;
    @Builder.Default
    @Positive(message = "size must be > 0")
    private Integer size = null;




}
