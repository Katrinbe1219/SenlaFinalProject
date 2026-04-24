package org.example.core.dto.getting.favourites;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavouriteFullDto {
    private Long id;
    private String goodName;
    private Long goodId;
    private Long userId;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant createdAt;
}
