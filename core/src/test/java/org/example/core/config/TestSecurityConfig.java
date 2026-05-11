package org.example.core.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.core.security.JwtCheckFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtCheckFilter jwtCheckFilter() throws ServletException, IOException {
        JwtCheckFilter filter = mock(JwtCheckFilter.class);
        doAnswer(invocation -> {
            HttpServletRequest request  = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            HttpSession session = request.getSession(false);
            if (session != null) {
                SecurityContext securityContext = (SecurityContext) session
                        .getAttribute("SPRING_SECURITY_CONTEXT");
                if (securityContext != null) {
                    HttpSessionSecurityContextRepository repo =
                            new HttpSessionSecurityContextRepository();
                    repo.saveContext(securityContext, request, response);
                    request.setAttribute(
                            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                            securityContext
                    );
                    SecurityContextHolder.setContext(securityContext);
                }
            }
            chain.doFilter(request, response);
            return null;
        }).when(filter).doFilter(any(),any(),any());
        return filter;
    }

    @Bean
    @Primary
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
}
