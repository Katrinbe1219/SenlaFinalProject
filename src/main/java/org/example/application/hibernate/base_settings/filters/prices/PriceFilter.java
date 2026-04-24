package org.example.application.hibernate.base_settings.filters.prices;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.hibernate.base_settings.sorting_types.PriceSortTypes;

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
    private BigDecimal  minPrice;
    private BigDecimal  maxPrice;
    private BigDecimal curPrice;

    //  mutually exclusive
    private Boolean current;
    private Boolean old;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate minDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate maxDate;

    @Builder.Default
    private PriceSortTypes sortDir = PriceSortTypes.ASC;

    @Builder.Default
    private  Integer page = 0;
    @Builder.Default
    private Integer size = 20;




}
