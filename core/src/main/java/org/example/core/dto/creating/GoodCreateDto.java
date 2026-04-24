package org.example.core.dto.creating;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.NullOrNotBlank;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodCreateDto {
    @NotBlank
    private String name;
    @NullOrNotBlank
    private String description;
    @Positive(message = "categoryId must be > 0")
    private Long categoryId;
    @Positive(message = "unitId must be > 0")
    @NotNull
    private Long unitId;
    private List<Long> tagsId;
}
