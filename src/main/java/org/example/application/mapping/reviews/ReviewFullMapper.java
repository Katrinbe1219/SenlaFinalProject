package org.example.application.mapping.reviews;

import org.example.application.dto.getting.reviews.ReviewFullDto;
import org.example.application.mapping.users.UserForReviewMapper;
import org.example.application.mapping.goods.GoodForReviewMapper;
import org.example.application.models.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {GoodForReviewMapper.class, UserForReviewMapper.class})
public interface ReviewFullMapper {

    ReviewFullDto toDto(Review review);
}
