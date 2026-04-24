package org.example.core.dto.patching;

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
    private Long id;
    private Long parentId;
}
