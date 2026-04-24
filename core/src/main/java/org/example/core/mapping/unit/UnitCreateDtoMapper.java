package org.example.core.mapping.unit;

import org.example.core.dto.creating.UnitCreateDto;
import org.example.core.models.Unit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnitCreateDtoMapper {
    Unit toEntity(UnitCreateDto dto);
}
