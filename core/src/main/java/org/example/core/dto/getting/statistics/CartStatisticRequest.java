package org.example.core.dto.getting.statistics;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartStatisticRequest {

    @NotNull(message = "goodIds must be given")
    @Size(min = 1, message = "goodIds length must be > 0")
    private List<Long> goodIds;

    @NotNull(message = "shopIds must be given")
    @Size(min = 1, message = "shopIds length must be > 0")
    private List<Long> shopIds;
}
