package org.example.core.dto.creating;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.models.types.ModeratorVerdict;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModeratorLogCreateDto {
    @Positive(message = "goodId must be > 0")
    private Long goodId;


    private ModeratorVerdict verdict;
    @NotBlank(message = "comment must be given")
    private String comment;
}
