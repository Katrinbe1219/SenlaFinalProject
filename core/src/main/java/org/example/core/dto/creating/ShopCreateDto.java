package org.example.core.dto.creating;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class ShopCreateDto {
    @NotBlank(message = "name can not be null")
    @Pattern(regexp = "^[\\p{L}\\s]+$")
    private String name;
    @NotBlank(message = "address can not be null")
    private String address;
    @Positive(message = "districtId must be >0")
    @NotNull(message = "districtId can not be null")
    private Long districtId;
}
