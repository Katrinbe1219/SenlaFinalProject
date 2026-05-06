package org.example.core.dto.getting.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.dto.TagDto;
import org.example.core.dto.UnitDto;
import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.models.types.GoodStatusFromModerator;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodGetFullDto {
    private Long id;
    private String name;
    private CategoryGetDto category;

    private UnitDto unit;

    private Double rate;
    private List<TagDto> tags;
    private String description;
    private String updatedAt;
    private String createdAt;
    private GoodStatusFromModerator moderatorStatus;
}
