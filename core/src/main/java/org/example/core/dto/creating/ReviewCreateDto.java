package org.example.core.dto.creating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.NullOrNotBlank;
import org.example.core.models.Good;
import org.example.core.models.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreateDto {
    @NullOrNotBlank
    private String review;
    @NotNull
    @Min(value = 0, message = "rate must be >= 0")
    @Max(value = 5, message = "rate must be <=5")
    private int rate;
}
