package org.example.application.hibernate.base_settings.filters.goods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.models.types.GoodStatusFromModerator;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodFilter {
    private List<Long> categoryIds;
    private List<Long> tagIds;
    //2 vs 1
    private Double minRating;
    private Double maxRating;
    private Double curRating;

    //2 vs 1
    private LocalDate minUpdatedAt;
    private LocalDate maxUpdatedAt;
    private LocalDate curUpdatedAt;

    //2 vs 1
    private LocalDate minCreatedAt;
    private LocalDate maxCreatedAt;
    private LocalDate curCreatedAt;

    private GoodStatusFromModerator status;


    @Builder.Default
    private String sortDir = "asc"; // by name
    @Builder.Default
    private  Integer page = 0;
    @Builder.Default
    private Integer size = 20;

}
