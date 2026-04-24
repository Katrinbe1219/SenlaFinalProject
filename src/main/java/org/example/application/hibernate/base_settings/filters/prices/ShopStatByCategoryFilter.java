package org.example.application.hibernate.base_settings.filters.prices;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopStatByCategoryFilter {
    private List<Long> categoryIds;
    private  List<Long> shopIds;
    // либо оба, либо текущая
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;
}

