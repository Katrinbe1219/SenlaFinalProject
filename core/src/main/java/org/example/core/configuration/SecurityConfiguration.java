package org.example.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.example.core.exceptions.CustomAccessDeniedHandler;
import org.example.core.security.*;
import org.example.core.services.auth.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration implements WebMvcConfigurer {

    private RefreshTokenService refreshTokenService;
    private DeviceInfoExtractor deviceInfoExtractor;


    public SecurityConfiguration( RefreshTokenService refreshTokenService, DeviceInfoExtractor deviceInfoExtractor) {
        this.deviceInfoExtractor = deviceInfoExtractor;
        this.refreshTokenService = refreshTokenService;
    }
    @Autowired
    JwtCheckFilter jwtCheckFilter;

    @Autowired
    JwtService jwtService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager manager,
                                                   ObjectMapper mapper
                                                   ) throws Exception {
        CustomLoginFilter filter = new CustomLoginFilter(manager, jwtService, refreshTokenService,
                deviceInfoExtractor, mapper);
        CustomAccessDeniedHandler customAccessDeniedHandler = new CustomAccessDeniedHandler();
        http.authorizeHttpRequests(auth ->
                    auth

                            .requestMatchers("/analyst/**").hasAnyRole("ADMIN", "ANALYST")
                            .requestMatchers("/categories/**", "/districts/**",
                                        "/tags/**", "/units/**",
                                    "/shops/**", "/goods/**","/prices/**").authenticated()
                            .requestMatchers("/favourites/**", "/reviews/**").hasAnyRole("MIN_USER", "MAX_USER")
                            .requestMatchers("/moderator/**").hasAnyRole("ADMIN", "MODERATOR")
                            .requestMatchers("/admin/**").hasRole("ADMIN")

                            .requestMatchers("/profile/**").hasAnyRole("ADMIN", "ANALYST", "MODERATOR", "MIN_USER", "MAX_USER")
                            .requestMatchers("/rates/**", "/export/**","/subscriptions/**").hasAnyRole("ADMIN", "ANALYST", "MODERATOR")
                            .requestMatchers("/login", "/refresh","/**").permitAll()

        )
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(ex ->
                        ex.accessDeniedHandler(customAccessDeniedHandler)
                                .authenticationEntryPoint((request, response, authException) -> {
                                    // для неаутентифицированных → 401 с JSON
                                    response.setContentType("application/json");
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.getWriter().write(
                                            new ObjectMapper().writeValueAsString(
                                                    Map.of("message", authException.getMessage())
                                            )
                                    );
                                })

        )
        .addFilterBefore(jwtCheckFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAt(filter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(new JwtRefreshFilter(refreshTokenService, deviceInfoExtractor, mapper), UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)  // явно убираем ненужное
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            BCryptPasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService
    ){

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return new ProviderManager(provider);
    }
}
