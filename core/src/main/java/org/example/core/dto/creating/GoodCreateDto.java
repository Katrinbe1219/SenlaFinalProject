package org.example.core.dto.creating;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.NullOrNotBlank;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodCreateDto {
    @NotBlank(message = "name must be given")
    private String name;
    @NullOrNotBlank
    private String description;
    @Positive(message = "categoryId must be > 0")
    @NotNull(message = "categoryId must be given")
    private Long categoryId;
    @Positive(message = "unitId must be > 0")
    @NotNull(message = "unit Id must not be null")
    private Long unitId;
    @Size(min = 1, message = "tagIds length must be > 0")
    private List<Long> tagIds;
}
