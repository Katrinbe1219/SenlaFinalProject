package org.example.application.dto.getting.prices;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PriceGetResultForModerator {
    private Long id;
    private BigDecimal price;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant validFrom;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant validTo;

    private Long goodId;
    private String goodName;

    private Long shopId;
    private String shopName;
    private String address;

    private String category;
    private Long categoryId;
}
