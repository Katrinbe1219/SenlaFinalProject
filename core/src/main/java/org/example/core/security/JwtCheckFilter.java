package org.example.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.PermissionDenied;
import org.example.core.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtCheckFilter extends OncePerRequestFilter {

    @Autowired
    JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractToken(request);
            if (token!=null){
                Claims claims = jwtService.parseToken(token);
                String username = claims.getSubject();
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            filterChain.doFilter(request, response);
        }catch (Exception e){
            if (e instanceof JwtException || e instanceof  IllegalArgumentException){
                sendError(e.getMessage(), response);
                logger.error("Проблема JwtCheckFilter doFilterInternal: " + e.getMessage());
            } else if (e instanceof NonHibernateException){
                sendError(e.getMessage(), response);
            }
            else throw e;

//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String url = request.getRequestURI();
        return url.equals("/login") || url.equals("/logout") || url.equals("/refresh");
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    private void sendError(String message, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(Map.of("error", message))
        );
        response.getWriter().flush();
        response.getWriter().close();
    }
}
