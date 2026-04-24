package org.example.core.dto.creating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.NullOrNotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitCreateDto {
    @NullOrNotBlank
    private String fullName;
    @NullOrNotBlank
    private String shortName;
}
