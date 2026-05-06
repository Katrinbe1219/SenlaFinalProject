package org.example.core.mapping;

import org.example.core.dto.getting.statistics.shops.ShopGetDto;
import org.example.core.models.Shop;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShopGetMapper {
    ShopGetDto toDto(Shop shop);
}
