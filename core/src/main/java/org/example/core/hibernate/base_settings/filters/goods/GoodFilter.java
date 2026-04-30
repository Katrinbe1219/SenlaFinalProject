package org.example.core.hibernate.base_settings.filters.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusiveExtended;
import org.example.core.models.types.GoodStatusFromModerator;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@MutuallyExclusiveExtended(first = "curRating", second="maxRating", third="minRating")
@MutuallyExclusiveExtended(first = "curUpdatedAt", second="maxUpdatedAt", third="minUpdatedAt")
@MutuallyExclusiveExtended(first = "curCreatedAt", second="maxCreatedAt", third="minCreatedAt")
public class GoodFilter {
    private List<Long> categoryIds;
    private List<Long> tagIds;
    //2 vs 1
    @PositiveOrZero(message = "minRating must be  >= 0")
    private Double minRating;
    @PositiveOrZero(message = "maxRating must be  >= 0")
    private Double maxRating;
    @PositiveOrZero(message = "curRating must be  >= 0")
    private Double curRating;

    //2 vs 1
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate minUpdatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate maxUpdatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate curUpdatedAt;

    //2 vs 1
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate minCreatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate maxCreatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate curCreatedAt;

    private GoodStatusFromModerator status;


    @Builder.Default
    @NotNull(message = "if you are undecided about the sortType, do not provide it")
    private String sortType = "asc"; // by name
    @Builder.Default
    @PositiveOrZero(message = "page must be >=0")
    private  Integer page = null;
    @Builder.Default
    @Positive(message = "size must be > 0")
    private Integer size = null;

}
