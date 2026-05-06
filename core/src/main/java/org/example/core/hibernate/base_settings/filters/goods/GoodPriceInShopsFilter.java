package org.example.core.hibernate.base_settings.filters.goods;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodPriceInShopsFilter {
    @Size(min = 1, message = "shopIds length must be > 0")
    private List<Long> shopIds;
    @Positive(message = "goodId must be > 0")
    @NotNull(message = "goodId must not be null")
    private Long goodId;
}
