package org.example.core.mapping.reviews;

import org.example.core.dto.getting.reviews.ReviewDto;
import org.example.core.models.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewForUserDtoMapper {

    ReviewDto toDto(Review review);
}
