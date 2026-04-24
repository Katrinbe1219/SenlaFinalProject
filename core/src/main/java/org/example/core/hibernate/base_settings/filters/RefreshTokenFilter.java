package org.example.core.hibernate.base_settings.filters;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusive;
import org.example.annotations.MutuallyExclusiveExtended;
import org.example.core.hibernate.base_settings.sorting_types.RefreshTokenSortType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@MutuallyExclusive(fields1 = "userId", fields2 = "userIds")
@MutuallyExclusiveExtended(first="expiresAt", second="startExpiresAt", third="endExpiresAt")
@MutuallyExclusiveExtended(first="createdAt", second="startCreatedAt", third="endCreatedAt")
@MutuallyExclusiveExtended(first="lastUsedAt", second="startLastUsedAt", third="endLastUsedAt")
public class RefreshTokenFilter {

    @Min(value = 1, message = "goodId > 0")
    private Long userId;
    private List<Long> userIds;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate expiresAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startExpiresAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endExpiresAt;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate createdAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startCreatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endCreatedAt;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate lastUsedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startLastUsedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate endLastUsedAt;

    @Builder.Default
    @PositiveOrZero( message = "page must be >=0")
    private Integer page = null;
    @Builder.Default
    @Positive( message = "size must be > 0")
    private Integer size = null;
    @Builder.Default
    private RefreshTokenSortType sortType = RefreshTokenSortType.ASC;

}
