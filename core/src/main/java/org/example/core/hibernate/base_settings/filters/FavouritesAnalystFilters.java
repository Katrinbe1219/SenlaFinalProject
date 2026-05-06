package org.example.core.hibernate.base_settings.filters;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavouritesAnalystFilters {
    @Size(min = 1, message = "goodIds size must be > 0")
    private List<Long> goodIds;
    @Size(min = 1, message = "categoryIds size must be > 0")
    private List<Long> categoryIds;
    @Size(min = 1, message = "tagIds size must be > 0")
    private List<Long> tagIds;
}
