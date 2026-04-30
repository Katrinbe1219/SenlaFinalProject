package org.example.core.dto.getting.goods;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GoodIdDto {
    @Positive(message = "id must be > 0")
    private Long id;
    private String name;
}
