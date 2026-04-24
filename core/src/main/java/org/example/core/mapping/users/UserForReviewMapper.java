package org.example.core.mapping.users;

import org.example.core.dto.getting.users.UserForReviewDto;
import org.example.core.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserForReviewMapper {
    @Mapping(source = "role.name", target = "role")
    UserForReviewDto toDto(User user);
}
