package org.example.application.mapping.unit;

import org.example.application.dto.UnitDto;
import org.example.application.models.Unit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UnitDtoMapper {
    UnitDto toDto(Unit unit);

    @Mapping(source = "id", target = "id", ignore = true)
    Unit toUnit(UnitDto unitDto);
}
