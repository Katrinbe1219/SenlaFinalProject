package org.example.application.dto.patching;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.models.types.GoodStatusFromModerator;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodPatchDto {
    private Long id;
    private String name;
    private String description;
    private List<Long> tagIds;
    private Long categoryId;
    private Long unitId;

}
