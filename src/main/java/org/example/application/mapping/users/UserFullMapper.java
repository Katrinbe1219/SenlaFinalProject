package org.example.application.mapping.users;

import org.example.application.dto.getting.users.UserFullDto;
import org.example.application.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserFullMapper {

    @Mapping(source = "role.id", target = "role.id")
    @Mapping(source="role.name", target = "role.name")
    UserFullDto toDto(User user);
}
