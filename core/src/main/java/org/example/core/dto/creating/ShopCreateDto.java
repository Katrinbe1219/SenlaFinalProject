package org.example.core.dto.creating;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class ShopCreateDto {
    @NotBlank
    private String name;
    @NotBlank
    private String address;
    @Positive(message = "districtId must be >0")
    private Long districtId;
}
