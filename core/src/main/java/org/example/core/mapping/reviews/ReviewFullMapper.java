package org.example.core.mapping.reviews;

import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.mapping.users.UserForReviewMapper;
import org.example.core.mapping.goods.GoodForReviewMapper;
import org.example.core.models.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {GoodForReviewMapper.class, UserForReviewMapper.class})
public interface ReviewFullMapper {

    ReviewFullDto toDto(Review review);
}
