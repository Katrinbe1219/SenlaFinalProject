package org.example.application.mapping.users;

import org.example.application.dto.getting.users.UserForModeratorDto;
import org.example.application.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserForModeratorMapper {
    @Mapping(source = "role.name", target="roleName")
    @Mapping(source = "role.id", target = "roleId")
    UserForModeratorDto toDto(User user);
}
