package org.example.core.mapping.goods;

import org.example.core.dto.getting.goods.GoodForReviewDto;
import org.example.core.models.Good;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GoodForReviewMapper {
    @Mapping(target = "averageRate", source = "rate")
    GoodForReviewDto toDto(Good good);
}
