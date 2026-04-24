package org.example.core.hibernate.base_settings.filters.users;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.MutuallyExclusive;
import org.example.annotations.MutuallyExclusiveExtended;
import org.example.core.hibernate.base_settings.sorting_types.UserForModeratorSortingType;
import org.example.core.models.types.RoleTypes;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MutuallyExclusiveExtended(first="updatedAt", second="startUpdatedAt", third="endUpdatedAt")
@MutuallyExclusiveExtended(first="createdAt", second="startCreatedAt", third="endCreatedAt")
@MutuallyExclusive(fields2 = "nonLocked", fields1 = "locked")
public class UserAdvancedFilter {
    private Boolean locked;
    private Boolean nonLocked;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startUpdatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private  LocalDate endUpdatedAt;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startCreatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private  LocalDate endCreatedAt;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate  updatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate createdAt;

    private RoleTypes roleType;

    @Builder.Default
    private UserForModeratorSortingType sortType = UserForModeratorSortingType.ASC;
}
