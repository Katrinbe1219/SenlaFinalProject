package org.example.core.mapping.goods;

import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.mapping.categories.CategoryGetDtoMapper;
import org.example.core.mapping.TagDtoMapper;
import org.example.core.mapping.unit.UnitDtoMapper;
import org.example.core.models.Good;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", uses = {CategoryGetDtoMapper.class,
        UnitDtoMapper.class, TagDtoMapper.class})
public interface GoodGetFullDtoMapper {


    @Mapping(source = "createdAt",
            target = "createdAt",
            qualifiedByName = "instantToMoscow")
    @Mapping(source = "updatedAt",
            target = "updatedAt",
            qualifiedByName = "instantToMoscow")
    GoodGetFullDto toDto(Good good);

    @Named("instantToMoscow")
    default String instantToMoscow(Instant instant) {
        if (instant == null) return null;

        return DateTimeFormatter
                .ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneId.of("Europe/Moscow"))
                .format(instant);
    }
}
