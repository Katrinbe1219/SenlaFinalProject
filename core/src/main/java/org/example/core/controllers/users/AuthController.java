package org.example.core.controllers.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.core.dto.auth.RegisterDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.security.DeviceInfoExtractor;
import org.example.core.security.TokenPair;
import org.example.core.services.auth.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private AuthService authService;
    private DeviceInfoExtractor deviceInfoExtractor;
    public AuthController(AuthService authService, DeviceInfoExtractor deviceInfoExtractor) {
        this.authService = authService;
        this.deviceInfoExtractor = deviceInfoExtractor;
    }

    @PostMapping("/register")
    public TokenPair register(
            HttpServletRequest request,
            @Valid  @RequestBody RegisterDto dto
            ){
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new NotCorrectInput("Password must be given");
        }

        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new NotCorrectInput("Username must be given");
        }

        if (dto.getLogin() == null || dto.getLogin().isEmpty()) {
            throw new NotCorrectInput("Login must be given");
        }
        String deviceInfo = deviceInfoExtractor.extract(request);

        return authService.register(dto, deviceInfo);


    }
}
