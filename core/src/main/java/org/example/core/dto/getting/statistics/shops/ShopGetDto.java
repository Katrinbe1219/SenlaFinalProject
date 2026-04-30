package org.example.core.dto.getting.statistics.shops;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopGetDto {
    @Positive(message = "id must be > 0")
    private Long id;
    private String name;
    private String address;
    private String district;
}
