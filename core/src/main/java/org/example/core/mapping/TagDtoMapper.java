package org.example.core.mapping;

import org.example.core.dto.TagDto;
import org.example.core.models.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagDtoMapper {

    TagDto toDto(Tag tag);

    @Mapping(target = "id", source = "id", ignore = true)
    Tag toTag(TagDto tagDto);

}
