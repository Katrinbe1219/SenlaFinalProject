package org.example.application.dto.getting.statistics.shops;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopGetDto {
    private Long id;
    private String name;
    private String address;
    private String district;
}
