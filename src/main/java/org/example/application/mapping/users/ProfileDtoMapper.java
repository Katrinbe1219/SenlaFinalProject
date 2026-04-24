package org.example.application.mapping.users;

import org.example.application.dto.getting.ProfileDto;
import org.example.application.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProfileDtoMapper {

    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "username",source = "." ,qualifiedByName = "getUsername")
    ProfileDto toDto(User user);

    @Named("getUsername")
    default  String getUsername(User user) {
        return user.getUsernameNotUserDetails();
    }
}
