package org.example.application.mapping.goods;

import org.example.application.dto.getting.goods.GoodForReviewDto;
import org.example.application.models.Good;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GoodForReviewMapper {
    @Mapping(target = "averageRate", source = "rate")
    GoodForReviewDto toDto(Good good);
}
