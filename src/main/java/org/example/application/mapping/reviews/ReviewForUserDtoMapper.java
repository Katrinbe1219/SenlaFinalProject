package org.example.application.mapping.reviews;

import org.example.application.dto.getting.reviews.ReviewDto;
import org.example.application.models.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewForUserDtoMapper {

    @Mapping(source = "good.name", target = "goodName")
    ReviewDto toDto(Review review);
}
