package org.example.application.hibernate.base_settings.filters.reviews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewForUserFilters {
    private Long goodId;
    private Integer rate;
    private LocalDate firstDate;
    private LocalDate lastDate;
    private LocalDate reviewDate;

    @Builder.Default
    private String sortDir = "asc";
    @Builder.Default
    private  Integer page = 0;
    @Builder.Default
    private Integer size = 4;
}
