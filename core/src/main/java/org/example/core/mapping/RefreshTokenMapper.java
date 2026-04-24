package org.example.core.mapping;

import org.example.core.dto.getting.RefreshTokenDto;
import org.example.core.models.RefreshToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {
    @Mapping(source = "user.id", target = "user.id")
    @Mapping(source = "user.username", target = "user.username")
    RefreshTokenDto toDto(RefreshToken refreshToken);
}
