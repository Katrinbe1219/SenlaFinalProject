package org.example.application.hibernate.base_settings.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.hibernate.base_settings.sorting_types.RefreshTokenSortType;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RefreshTokenFilter {
    private Long userId;
    private List<Long> userIds;

    private LocalDate expiresAt;
    private LocalDate startExpiresAt;
    private LocalDate endExpiresAt;

    private LocalDate createdAt;
    private LocalDate startCreatedAt;
    private LocalDate endCreatedAt;

    private LocalDate lastUsedAt;
    private LocalDate startLastUsedAt;
    private LocalDate endLastUsedAt;

    @Builder.Default
    private Integer page = null;
    @Builder.Default
    private Integer size = null;
    @Builder.Default
    private RefreshTokenSortType sortType = RefreshTokenSortType.ASC;

}
