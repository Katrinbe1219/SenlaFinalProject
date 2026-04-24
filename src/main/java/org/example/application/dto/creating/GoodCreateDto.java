package org.example.application.dto.creating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodCreateDto {
    private String name;
    private String description;
    private Long categoryId;
    private Long unitId;
    private List<Long> tagsId;
}
