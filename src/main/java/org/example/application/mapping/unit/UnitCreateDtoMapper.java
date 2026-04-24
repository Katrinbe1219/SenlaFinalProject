package org.example.application.mapping.unit;

import org.example.application.dto.creating.UnitCreateDto;
import org.example.application.models.Unit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnitCreateDtoMapper {
    Unit toEntity(UnitCreateDto dto);
}
