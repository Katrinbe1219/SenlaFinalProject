package org.example.core.hibernate.base_settings.filters.prices;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusive;
import org.example.annotations.ValidDateRange;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MutuallyExclusive(fields1 = "categoryIds", fields2 = "goodIds")
@MutuallyExclusive(fields1 = "categoryIds", fields2 = "tagIds")
@MutuallyExclusive(fields1 = "tagIds", fields2 = "goodIds")
@ValidDateRange(first = "startDate", second="endDate")
public class DistrictStatisticFilter {
    private List<Long> districtIds;

    // mutually exclusive
    private List<Long> categoryIds;
    private List<Long> goodIds;
    private List<Long> tagIds;

    // either both or current
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;



}
