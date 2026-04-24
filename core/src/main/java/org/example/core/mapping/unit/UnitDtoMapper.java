package org.example.core.mapping.unit;

import org.example.core.dto.UnitDto;
import org.example.core.models.Unit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UnitDtoMapper {
    UnitDto toDto(Unit unit);

    @Mapping(source = "id", target = "id", ignore = true)
    Unit toUnit(UnitDto unitDto);
}
