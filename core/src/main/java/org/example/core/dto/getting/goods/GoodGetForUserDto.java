package org.example.core.dto.getting.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.dto.TagDto;
import org.example.core.dto.UnitDto;
import org.example.core.dto.getting.categories.CategorySmallDto;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodGetForUserDto {

    private Long id;
    private String name;
    private CategorySmallDto category;
    private UnitDto unit;
    private BigDecimal rate;
    private List<TagDto> tags;
    private String description;

}
