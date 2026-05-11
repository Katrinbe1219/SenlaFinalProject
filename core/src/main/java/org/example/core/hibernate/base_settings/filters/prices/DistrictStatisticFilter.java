package org.example.core.hibernate.base_settings.filters.prices;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
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
    @Size(min=1, message = "districtIds length must  be > 0")
    private List<Long> districtIds;

    // mutually exclusive
    @Size(min=1, message = "categoryIds length must  be > 0")
    private List<Long> categoryIds;
    @Size(min=1, message = "goodIds length must  be > 0")
    private List<Long> goodIds;
    @Size(min=1, message = "tagIds length must  be > 0")
    private List<Long> tagIds;

    // either both or current
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;



}
