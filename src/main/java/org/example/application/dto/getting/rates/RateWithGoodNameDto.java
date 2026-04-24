package org.example.application.dto.getting.rates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateWithGoodNameDto {
    private String goodName;
    private Double rate;
}
