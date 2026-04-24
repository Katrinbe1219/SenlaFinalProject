package org.example.core.mapping.users;

import org.example.core.dto.getting.users.UserFullDto;
import org.example.core.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserFullMapper {

    @Mapping(source = "role.id", target = "role.id")
    @Mapping(source="role.name", target = "role.name")
    @Mapping(qualifiedByName = "getUsername", target = "username", source = ".")
    UserFullDto toDto(User user);

    @Named("getUsername")
    default String getUsername(User user){
        return user.getUsernameNotUserDetails();
    };
}
