package org.example.application.dto.getting.favourites;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavouriteGetForUserDto {
    private String goodName;
    private Long goodId;
}
