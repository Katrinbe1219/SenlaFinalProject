package org.example.core.dto.getting.prices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceGetDtoForUser {
    private String shopName;
    private String address;

    private String goodName;
    private int price;
}
