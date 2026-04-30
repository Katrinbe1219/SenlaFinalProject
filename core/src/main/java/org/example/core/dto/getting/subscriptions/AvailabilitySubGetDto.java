package org.example.core.dto.getting.subscriptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.dto.export.ModeratorDto;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.getting.statistics.shops.ShopGetDto;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilitySubGetDto {
    private Long id;
    private ModeratorDto user;
    private GoodIdDto good;
    private ShopGetDto shop;
    @JsonFormat(pattern = "dd.MM.yyyy", timezone = "Europe/Moscow")
    private Instant createdAt;
}
