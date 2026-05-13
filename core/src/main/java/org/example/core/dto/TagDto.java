package org.example.core.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;
import org.example.annotations.NullOrNotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TagDto {
    @NullOrNotBlank(message = "name can not be blank")
    private String name;
    @Positive(message = "id must be > 0")
    private Long id;
}
