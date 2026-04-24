package org.example.core.hibernate.base_settings.filters.prices;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class PriceFilter {
    private List<Long> categoriesId;
    private List<Long> shopsId;

    //  взаимоисключающие
    private List<Long> goodsId;
    private List<Long> districtsId;

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
    private LocalDate minDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate maxDate;

    @Builder.Default
    private PriceSortTypes sortDir = PriceSortTypes.ASC;

    @Builder.Default
    @PositiveOrZero(message = "page must be >=0")
    private  Integer page = 0;
    @Builder.Default
    @Positive(message = "size must be > 0")
    private Integer size = 20;




}
