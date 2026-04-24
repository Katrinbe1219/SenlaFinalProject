package org.example.application.hibernate.base_settings.filters;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.hibernate.base_settings.sorting_types.UserForModeratorSortingType;
import org.example.application.models.types.RoleTypes;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserForModeratorFilter {
    private Boolean locked;
    private Boolean nonLocked;

    private LocalDate startUpdatedDate;
    private  LocalDate endUpdatedDate;

    private LocalDate startCreatedDate;
    private  LocalDate endCreatedDate;

    private LocalDate  updatedAt;
    private LocalDate createdAt;

    private RoleTypes roleType;

    @Builder.Default
    private UserForModeratorSortingType sortType = UserForModeratorSortingType.ASC;
}
