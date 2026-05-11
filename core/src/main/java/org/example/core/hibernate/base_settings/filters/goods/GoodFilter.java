package org.example.core.hibernate.base_settings.filters.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusiveExtended;
import org.example.annotations.ValidDateRange;
import org.example.annotations.ValidDifference;
import org.example.core.hibernate.base_settings.sorting_types.GoodSortType;
import org.example.core.models.types.GoodStatusFromModerator;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@MutuallyExclusiveExtended(first = "curRating", second="maxRating", third="minRating")
@MutuallyExclusiveExtended(first = "curUpdatedAt", second="startUpdatedAt", third="endUpdatedAt")
@MutuallyExclusiveExtended(first = "curCreatedAt", second="startCreatedAt", third="endCreatedAt")
@ValidDateRange(first = "startCreatedAt", second = "endCreatedAt")
@ValidDateRange(first = "startUpdatedAt", second = "endUpdatedAt")
@ValidDifference(first = "minRating", second = "maxRating")
public class GoodFilter {
    @Size(min=1, message = "categoryIds must be > 0")
    private List<Long> categoryIds;
    @Size(min=1, message = "categoryIds must be > 0")
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
    private LocalDate endUpdatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startUpdatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate curUpdatedAt;

    //2 vs 1
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startCreatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endCreatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate curCreatedAt;

    private GoodStatusFromModerator status;


    @Builder.Default
    @NotNull(message = "if you are undecided about the sortType, do not provide it")
    private GoodSortType sortType = GoodSortType.ASC; // by name
    @Builder.Default
    @PositiveOrZero(message = "page must be >=0")
    private  Integer page = 0;
    @Builder.Default
    @Positive(message = "size must be > 0")
    private Integer size = null;

}
