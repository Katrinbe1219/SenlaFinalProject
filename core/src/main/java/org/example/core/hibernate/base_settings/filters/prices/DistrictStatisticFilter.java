package org.example.core.hibernate.base_settings.filters.prices;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusive;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MutuallyExclusive(fields1 = "categoriesId", fields2 = "goodsId")
@MutuallyExclusive(fields1 = "categoriesId", fields2 = "tagsIds")
@MutuallyExclusive(fields1 = "tagsIds", fields2 = "goodsId")
public class DistrictStatisticFilter {
    private List<Long> districtsId;

    // mutually exclusive
    private List<Long> categoriesId;
    private List<Long> goodsId;
    private List<Long> tagsIds;

    // either both or current
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;



}
