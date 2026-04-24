package org.example.core.dto.getting.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodPriceInShop {
    private String shopName;
    private Double price;
}
