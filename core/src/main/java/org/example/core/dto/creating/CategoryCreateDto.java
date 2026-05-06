package org.example.core.dto.creating;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class CategoryCreateDto {
    @NotBlank
    private String name;
    @Positive(message = "parentId must be > 0" )
    private Long parentId;
}
