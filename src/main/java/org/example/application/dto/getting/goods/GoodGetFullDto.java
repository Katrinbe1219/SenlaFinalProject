package org.example.application.dto.getting.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.dto.TagDto;
import org.example.application.dto.UnitDto;
import org.example.application.dto.getting.CategoryGetDto;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodGetFullDto {
    private Long id;
    private String name;
    private CategoryGetDto category;
    private String parentCategoryName;
    private UnitDto unit;

    private Double rate;
    private List<TagDto> tags;
    private String description;
    private String updatedAt;
    private String createdAt;
}
