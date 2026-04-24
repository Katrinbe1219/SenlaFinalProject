package org.example.application.mapping;

import org.example.application.dto.TagDto;
import org.example.application.models.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagDtoMapper {

    TagDto toDto(Tag tag);

    @Mapping(target = "id", source = "id", ignore = true)
    Tag toTag(TagDto tagDto);

}
