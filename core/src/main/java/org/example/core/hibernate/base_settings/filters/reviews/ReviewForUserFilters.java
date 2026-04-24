package org.example.core.hibernate.base_settings.filters.reviews;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusiveExtended;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@MutuallyExclusiveExtended(first = "reviewDate", second="lastDate", third="firstDate")
public class ReviewForUserFilters {
    @Positive(message = "goodId must be  > 0")
    private Long goodId;
    @Min(value = 0, message = "rate must be >=0")
    @Max(value =5, message = "rate must be  <=5")
    private Double rate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate firstDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate lastDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate reviewDate;

    @Builder.Default
    private String sortDir = "asc";
    @Builder.Default
    @PositiveOrZero(message = "page must be >=0")
    private  Integer page = null;
    @Builder.Default
    @Positive(message = "size must be >0")
    private Integer size = null;
}
