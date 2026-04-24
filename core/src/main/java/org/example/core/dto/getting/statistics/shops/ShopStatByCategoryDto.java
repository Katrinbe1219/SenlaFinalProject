package org.example.core.dto.getting.statistics.shops;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.dto.getting.statistics.categories.CategoryStatDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopStatByCategoryDto {
    private Long shopId;
    private String shopName;
    private List<CategoryStatDto> categories;
}
