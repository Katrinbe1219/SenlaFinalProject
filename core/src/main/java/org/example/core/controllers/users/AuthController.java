package org.example.core.controllers.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.core.dto.auth.RegisterDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.security.DeviceInfoExtractor;
import org.example.core.security.TokenPair;
import org.example.core.services.auth.AuthService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("!isAuthenticated()")
    public TokenPair register(
            HttpServletRequest request,
            @Valid  @RequestBody RegisterDto dto
            ){
        String deviceInfo = deviceInfoExtractor.extract(request);
        return authService.register(dto, deviceInfo);


    }
}
