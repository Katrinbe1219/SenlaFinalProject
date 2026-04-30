package org.example.core.mapping.subscriptions;

import org.example.core.dto.getting.subscriptions.AvailabilitySubGetDto;
import org.example.core.models.AvailabilitySubscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvailabilitySubGetMapper {
    @Mapping(target = "shop.district", source = "shop.district.name")
    AvailabilitySubGetDto toDto(AvailabilitySubscription old);
}
