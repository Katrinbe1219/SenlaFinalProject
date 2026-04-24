package org.example.core.mapping.categories;

import org.example.core.dto.getting.CategoryGetDto;
import org.example.core.models.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryGetDtoMapper {
    @Mapping(source = "parent.name", target = "parent")
    CategoryGetDto toCategoryGetDto(Category category);

}
