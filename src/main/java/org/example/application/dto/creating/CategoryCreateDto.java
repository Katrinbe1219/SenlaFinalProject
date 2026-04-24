package org.example.application.dto.creating;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class CategoryCreateDto {
    private String name;
    private Long parentId;
}
