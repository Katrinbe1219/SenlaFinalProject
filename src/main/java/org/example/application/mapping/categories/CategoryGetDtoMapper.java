package org.example.application.mapping.categories;

import org.example.application.dto.getting.CategoryGetDto;
import org.example.application.models.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryGetDtoMapper {
    @Mapping(source = "parent.name", target = "parent")
    CategoryGetDto toCategoryGetDto(Category category);

}
