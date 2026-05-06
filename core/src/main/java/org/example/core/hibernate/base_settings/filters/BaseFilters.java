package org.example.core.hibernate.base_settings.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseFilters {
    private List<Long> ids;
    private List<Long> dependencyIds;
}
