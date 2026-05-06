package org.example.core.dto.patching;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.NullOrNotBlank;
import org.example.core.models.types.GoodStatusFromModerator;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodPatchDto {
    private Long id;
    @NullOrNotBlank
    private String name;
    @NullOrNotBlank
    private String description;

    private List<Long> tagIds;
    @Positive(message = "categoryId must be >0")
    private Long categoryId;
    @Positive(message = "unitId must be >0")
    private Long unitId;

}
