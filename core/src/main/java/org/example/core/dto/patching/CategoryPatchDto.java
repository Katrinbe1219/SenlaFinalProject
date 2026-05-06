package org.example.core.dto.patching;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.NullOrNotBlank;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPatchDto {
    @NullOrNotBlank
    private String name;
    @Positive(message = "id must be > 0")
    @NotNull(message = "id can not be null")
    private Long id;
    @Positive(message = "parentId must be > 0")
    private Long parentId;
}
