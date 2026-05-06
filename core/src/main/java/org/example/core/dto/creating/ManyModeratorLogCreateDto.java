package org.example.core.dto.creating;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManyModeratorLogCreateDto {
    @NotNull(message = "verdicts must be given")
    @Size(min = 1, message = "verdicts must be given")
    private List<ModeratorLogCreateDto> verdicts;
}
