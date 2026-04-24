package org.example.application.mapping.users;

import org.example.application.dto.getting.users.UserForReviewDto;
import org.example.application.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserForReviewMapper {
    @Mapping(source = "role.name", target = "role")
    UserForReviewDto toDto(User user);
}
