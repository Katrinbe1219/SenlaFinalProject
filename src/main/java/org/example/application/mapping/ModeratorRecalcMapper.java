package org.example.application.mapping;

import org.example.application.dto.getting.ModeratorRecalcDto;
import org.example.application.models.ModeratorRatingCheck;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModeratorRecalcMapper {
    @Mapping(source = "good.name", target="good.name")
    @Mapping(source = "good.id", target = "good.id")
    @Mapping(target = "moderator.id", source = "moderator.id")
    @Mapping(target = "moderator.username", source = "moderator.username")
    ModeratorRecalcDto toDto(ModeratorRatingCheck original);

}
