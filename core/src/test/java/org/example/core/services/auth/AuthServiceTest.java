package org.example.core.services.auth;

import org.example.core.dto.auth.RegisterDto;
import org.example.core.exceptions.RegistrationException;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.models.Role;
import org.example.core.models.User;
import org.example.core.security.JwtService;
import org.example.core.security.TokenPair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    UserHibImpl userHib;
    @Mock
    JwtService jwtService;
    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RefreshTokenService refreshTokenService;

    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("registerIfConflicts")
    @Tag("negative")
    void registerIfConflicts(){
        User user = new User();
        user.setEmail("salhof");
        user.setLogin("login");


        RegisterDto dto = new RegisterDto();
        dto.setUsername("username");
        dto.setEmail("salhof");
        dto.setLogin("login");

        when(userHib.findByUsernameOrLoginOrEmail(anyString(), anyString(), anyString()))
                .thenReturn(List.of(user));
        RegistrationException ex = assertThrows(RegistrationException.class,
                () -> authService.register(dto, "device"));

        assertTrue(ex.getErrors().containsKey("login"));
        assertTrue(ex.getErrors().containsKey("email"));
        assertFalse(ex.getErrors().containsKey("username"));

    }

    @Test
    @DisplayName("registerIfSuccessful")
    @Tag("positive")
    void registerIfSuccessful(){


        RegisterDto dto = new RegisterDto();
        dto.setUsername("username");
        dto.setEmail("salhof");
        dto.setLogin("login");

        when(userHib.findByUsernameOrLoginOrEmail(anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(userHib.getDefaultRole()).thenReturn(new Role());
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenService.createToken(any(User.class), anyString())).thenReturn("refresh-token");

        TokenPair res = authService.register(dto, "device");
        assertEquals("access-token", res.accessToken());
        assertEquals("refresh-token", res.refreshToken());

    }
}
