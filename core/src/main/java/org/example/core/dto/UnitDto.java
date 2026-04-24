package org.example.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitDto {
    private Long id;
    private String fullName;
    private String shortName;
}
