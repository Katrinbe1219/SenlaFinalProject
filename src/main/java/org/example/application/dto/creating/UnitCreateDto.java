package org.example.application.dto.creating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitCreateDto {
    private String fullName;
    private String shortName;
}
