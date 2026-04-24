package org.example.application.services.auth;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.application.dto.auth.RegisterDto;
import org.example.application.exceptions.RegistrationException;
import org.example.application.hibernate.objects.UserHibImpl;
import org.example.application.models.User;
import org.example.application.security.JwtService;
import org.example.application.security.TokenPair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger= LogManager.getLogger(AuthService.class);
    private UserHibImpl userHib;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;

    public AuthService(UserHibImpl userHib, PasswordEncoder passwordEncoder,
                       JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userHib = userHib;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public TokenPair register(RegisterDto dto, String deviceInfo){
        List<User> conflicts = userHib.findByUsernameOrLoginOrEmail(dto.getLogin(), dto.getUsername(), dto.getEmail());
        if(!conflicts.isEmpty()){
            Map<String, String> errors = new HashMap<>();
            boolean loginConflict = conflicts.stream().anyMatch(s -> dto.getLogin().equals(s.getLogin()));
            boolean emailConflict = conflicts.stream().anyMatch(s -> s.getEmail() != null &&  dto.getEmail().equals(s.getEmail()));
            boolean usernameConflict = conflicts.stream().anyMatch(s -> dto.getUsername().equals(s.getUsernameNotUserDetails()));

            if (loginConflict) errors.put("login", "already exists");
            if (emailConflict) errors.put("email", "already exists");
            if (usernameConflict) errors.put("username","already exists");
            throw new RegistrationException(errors);


        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setLogin(dto.getLogin());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNonLocked(true);
        user.setRole(userHib.getDefaultRole());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userHib.save(user, logger);

        String token = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createToken(user, deviceInfo);
        return new TokenPair(token, refreshToken);



    }
}
