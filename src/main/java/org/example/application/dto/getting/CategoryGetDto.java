package org.example.application.dto.getting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryGetDto {
    private String name;
    private Long id;
    private String parent;
}
