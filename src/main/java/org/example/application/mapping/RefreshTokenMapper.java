package org.example.application.mapping;

import org.example.application.dto.getting.RefreshTokenDto;
import org.example.application.models.RefreshToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {
    @Mapping(source = "user.id", target = "user.id")
    @Mapping(source = "user.username", target = "user.username")
    RefreshTokenDto toDto(RefreshToken refreshToken);
}
