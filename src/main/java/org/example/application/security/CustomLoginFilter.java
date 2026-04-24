package org.example.application.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.application.models.User;
import org.example.application.services.auth.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

// @Component - может использовать не мой конструктор
public class CustomLoginFilter extends BasicAuthenticationFilter {
    private  static final Logger logger = LogManager.getLogger(CustomLoginFilter.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    private DeviceInfoExtractor deviceInfoExtractor;

    public CustomLoginFilter(AuthenticationManager authenticationManager, JwtService jwtService,
                             RefreshTokenService refreshTokenService, DeviceInfoExtractor deviceInfoExtractor) {
        super(authenticationManager);
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.deviceInfoExtractor = deviceInfoExtractor;

    }

    @Override
    protected  void onSuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication auth
    ) throws IOException {
        try {
            String token = jwtService.generateToken(auth);
            String deviceInfo = deviceInfoExtractor.extract(request);
            User user = (User)  auth.getPrincipal();
            String refreshToken = refreshTokenService.createToken(user, deviceInfo);

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);


            response.getWriter().write(mapper.writeValueAsString(
                    new TokenPair(token, refreshToken)
            ));

            response.getWriter().flush();
            response.getWriter().close();
        }catch (Exception e){
            logger.error("CustomLoginFilter " + e.getMessage());
            throw new IOException(e);
        }




    }

    @Override
    protected void onUnsuccessfulAuthentication(HttpServletRequest request,
                                                HttpServletResponse response,
                                                AuthenticationException failed) throws IOException {
        response.setContentType("application/json");


        if (failed instanceof LockedException) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(mapper.writeValueAsString(
                    Map.of("error", "Your account is locked")
            ));
        }else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(mapper.writeValueAsString(
                    Map.of("error", "Bad credentials")
            ));
        }

        response.getWriter().flush();
        response.getWriter().close();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException, ServletException {
        if (!request.getRequestURI().equals("/login")) {
            chain.doFilter(request, response);
            return;
        }
        super.doFilterInternal(request, response, chain);
    }
}
