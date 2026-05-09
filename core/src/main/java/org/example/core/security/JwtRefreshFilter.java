package org.example.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.core.services.auth.RefreshTokenService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtRefreshFilter extends OncePerRequestFilter {
    private RefreshTokenService refreshTokenService;
    private DeviceInfoExtractor deviceInfoExtractor;
    private ObjectMapper mapper;

    public JwtRefreshFilter(RefreshTokenService refreshTokenService, DeviceInfoExtractor deviceInfoExtractor, ObjectMapper mapper) {
        this.refreshTokenService = refreshTokenService;
        this.deviceInfoExtractor = deviceInfoExtractor;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!request.getRequestURI().equals("/refresh")
                || !request.getMethod().equals("POST")) {
            chain.doFilter(request, response);
            return;
        }

        try{
            RefreshRequest body = mapper.readValue(
                    request.getInputStream(), RefreshRequest.class
            );

            String deviceInfo = deviceInfoExtractor.extract(request);
            TokenPair pair = refreshTokenService.rotate(body.refreshToken(), deviceInfo);

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(mapper.writeValueAsString(pair));
        }catch (Exception e){
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(mapper.writeValueAsString(e));
        }finally {
            response.getWriter().flush();
            response.getWriter().close();
        }

    }
}
