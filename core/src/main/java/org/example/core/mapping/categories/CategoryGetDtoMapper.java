package org.example.core.mapping.categories;

import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.models.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryGetDtoMapper {
    @Mapping(source = "parent.name", target = "parentName")
    @Mapping(source = "parent.id", target = "parentId")
    CategoryGetDto toCategoryGetDto(Category category);

}
