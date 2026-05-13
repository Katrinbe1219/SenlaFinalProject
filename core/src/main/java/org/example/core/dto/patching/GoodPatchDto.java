package org.example.core.dto.patching;

import jakarta.validation.constraints.Pattern;
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
    //// ^[\\p{L}]+$" - любые языки мира, \\p{L} - unicode Letter
    @Pattern(regexp = ".*\\p{L}.*", message = "Name must contain letters")
    private String name;
    @NullOrNotBlank(message = "description can not be blank")
    private String description;

    private List<Long> tagIds;
    @Positive(message = "categoryId must be >0")
    private Long categoryId;
    @Positive(message = "unitId must be >0")
    private Long unitId;

}
