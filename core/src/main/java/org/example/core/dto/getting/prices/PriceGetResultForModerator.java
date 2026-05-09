package org.example.core.dto.getting.prices;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PriceGetResultForModerator {
    @Positive(message = "id must be >0")
    private Long id;
    @PositiveOrZero(message = "price must be >= 0")
    private BigDecimal price;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant validFrom;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant validTo;

    @Positive(message = "id must be >0")
    private Long goodId;
    private String goodName;

    @Positive(message = "shopId must be >0")
    private Long shopId;
    private String shopName;
    private String address;

    private String category;
    @Positive(message = "categoryId must be >0")
    private Long categoryId;
}
