package org.example.core.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TagDto {
    private String name;
    @Positive(message = "id must be > 0")
    private Long id;
}
