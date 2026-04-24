package org.example.application.hibernate.base_settings.filters.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodPriceInShopsFilter {
    private List<Long> shopIds;
    private Long goodId;
}
