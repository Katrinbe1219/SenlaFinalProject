package org.example.core.mapping.subscriptions;

import org.example.core.dto.getting.subscriptions.PriceSubGetDto;
import org.example.core.models.PriceSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PriceSubGetMapper {

    @Mapping(source = "shop.district.name", target = "shop.district")
    PriceSubGetDto toDto(PriceSubscription old);
}
