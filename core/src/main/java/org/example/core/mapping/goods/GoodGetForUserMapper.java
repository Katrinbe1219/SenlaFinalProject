package org.example.core.mapping.goods;

import org.example.core.dto.getting.goods.GoodGetForUserDto;
import org.example.core.models.Good;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GoodGetForUserMapper {

    GoodGetForUserDto toDto(Good old);
}
