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
public class DistrictStatisticFilter {
    private List<Long> districtsId;

    // mutually exclusive
    private List<Long> categoriesId;
    private List<Long> goodsId;
    private List<Long> tagsIds;

    // either both or current
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;



}
